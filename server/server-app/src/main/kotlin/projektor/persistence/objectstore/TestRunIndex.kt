package projektor.persistence.objectstore

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import projektor.objectstore.json.JsonObjectStore
import projektor.server.api.PublicId
import java.net.URLDecoder
import java.net.URLEncoder
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors

/**
 * Indexes that stand in for the database's queries across test runs:
 *
 * - Repository timeline: one shard document per repository per month, with an entry for each test run that has
 *   Git metadata. Repository-level queries read a repository's shards and filter the entries in memory, which stays
 *   cheap because test run cleanup bounds how many months of shards a repository has.
 * - Created date: a marker per test run under its created date, for test run cleanup (which also covers test runs
 *   without Git metadata).
 * - Organization: a marker per repository under its organization.
 *
 * Index entries are never patched incrementally. Every change to a test run's data calls [refreshEntry], which
 * rebuilds the run's entry from its source documents inside the shard's compare-and-swap update. Concurrent updates
 * therefore always leave the entry matching the latest source documents.
 *
 * Refreshes of the same shard within this server are queued and applied together in one compare-and-swap update,
 * so parallel uploads to a repository don't conflict with each other; only updates from other server instances can.
 *
 * The indexes are derived data, so failing to update them doesn't fail the change that triggered the update.
 * Instead the test run gets a repair marker, and [repairIndex] (run periodically) rebuilds its index entries.
 */
class TestRunIndex(val documentStore: TestRunDocumentStore) {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    private val store: JsonObjectStore = documentStore.store

    private val shardBatchers = Array(SHARD_BATCHER_STRIPES) { ShardBatcher() }

    /**
     * Records that the test run's index entries may need rebuilding. Written before saving a new test run, so a
     * crash between saving it and indexing it is repaired too. Returns the marker's key for [clearRepairMarker].
     */
    suspend fun writeRepairMarker(
        publicId: PublicId,
        testRun: TestRunDocument?,
        savingTestRun: Boolean,
    ): String =
        withContext(Dispatchers.IO) {
            val key = "$INDEX_REPAIR_PREFIX${publicId.id}-${UUID.randomUUID()}.json"
            store.write(
                key,
                IndexRepairDocument(
                    publicId = publicId.id,
                    repoName = testRun?.gitMetadata?.repoName,
                    createdTimestamp = testRun?.summary?.createdTimestamp,
                    markedAt = Instant.now(),
                    savingTestRun = savingTestRun,
                ),
            )
            key
        }

    suspend fun clearRepairMarker(key: String) {
        try {
            withContext(Dispatchers.IO) { store.delete(key) }
        } catch (e: Exception) {
            // The repair job deletes it after finding nothing to repair
            logger.warn("Failed to delete index repair marker $key", e)
        }
    }

    /**
     * Adds a newly saved test run to the indexes. Returns whether every index was updated; when not, the repair
     * marker the caller wrote before saving the test run (see [writeRepairMarker]) gets the test run indexed later.
     */
    suspend fun addTestRun(
        publicId: PublicId,
        testRun: TestRunDocument,
    ): Boolean =
        try {
            indexTestRun(publicId, testRun)
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to index new test run $publicId, leaving it for the index repair job", e)
            false
        }

    /**
     * Removes a test run that's being deleted from the repository timeline. Takes the test run document as it was
     * before it was deleted, which locates the run's timeline entry.
     */
    suspend fun removeTestRun(
        publicId: PublicId,
        deletedTestRun: TestRunDocument,
    ) {
        refreshEntry(publicId, deletedTestRun)
    }

    /**
     * Removes the test run's created date marker, the last step of deleting a test run: until then, test run cleanup
     * keeps finding the test run and retries the delete.
     */
    suspend fun removeCreatedMarker(
        publicId: PublicId,
        createdTimestamp: Instant,
    ) {
        withContext(Dispatchers.IO) {
            store.delete(createdMarkerKey(publicId, createdTimestamp))
        }
    }

    /**
     * Rebuilds the test run's repository timeline entry from its current documents, removing the entry if the
     * test run no longer exists. Does nothing for test runs without Git metadata, which aren't in the timeline.
     * If the update fails, marks the test run for repair instead of throwing.
     *
     * @param knownTestRun the test run document if the caller already has it, which saves reading it to find
     * the entry's shard. Required when the test run has been deleted.
     */
    suspend fun refreshEntry(
        publicId: PublicId,
        knownTestRun: TestRunDocument? = null,
    ) {
        var testRunForShard = knownTestRun

        try {
            testRunForShard = testRunForShard ?: documentStore.readTestRunIncludingDeleted(publicId) ?: return
            val git = testRunForShard.gitMetadata ?: return

            refreshShardEntry(publicId, git.repoName, testRunForShard.summary.createdTimestamp)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            markForRepairAfterFailure(publicId, testRunForShard, e)
        }
    }

    /**
     * Rebuilds the index entries of every test run with a repair marker. Returns the number of markers resolved.
     */
    suspend fun repairIndex(): Int {
        val markerKeys = withContext(Dispatchers.IO) { store.list(INDEX_REPAIR_PREFIX).objectNames }
        var resolvedCount = 0

        markerKeys.forEach { markerKey ->
            try {
                val marker = withContext(Dispatchers.IO) { store.read<IndexRepairDocument>(markerKey) }

                if (marker == null || repairTestRun(marker)) {
                    withContext(Dispatchers.IO) { store.delete(markerKey) }
                    resolvedCount++
                }
            } catch (e: Exception) {
                logger.warn("Failed to repair index from marker $markerKey, will retry", e)
            }
        }

        if (markerKeys.isNotEmpty()) {
            logger.info("Repaired index entries for $resolvedCount of ${markerKeys.size} test runs marked for repair")
        }

        return resolvedCount
    }

    /**
     * All of the repository's timeline entries, oldest first. Runs created at the same time are ordered
     * by when they were indexed, like the database orders them by their generated IDs.
     */
    suspend fun readRepositoryEntries(repoName: String): List<TestRunIndexEntry> =
        coroutineScope {
            val shardKeys = withContext(Dispatchers.IO) { store.list(timelineShardPrefix(repoName)).objectNames }

            shardKeys
                .map { shardKey -> async(Dispatchers.IO) { store.read<TimelineShardDocument>(shardKey) } }
                .awaitAll()
                // Shard keys sort by month and entries within a shard are kept in order
                .flatMap { it?.entries.orEmpty() }
        }

    suspend fun readOrganizationRepositoryNames(orgName: String): List<String> =
        coroutineScope {
            val markerKeys = withContext(Dispatchers.IO) { store.list(organizationPrefix(orgName)).objectNames }

            markerKeys.map { key -> decodeKeySegment(key.removePrefix(organizationPrefix(orgName)).removeSuffix(".json")) }
        }

    /**
     * Test runs created at or before the given time, oldest first.
     */
    suspend fun readTestRunsCreatedAtOrBefore(createdBefore: Instant): List<CreatedMarkerDocument> =
        coroutineScope {
            val lastDay = LocalDate.ofInstant(createdBefore, ZoneOffset.UTC)

            val dayPrefixes =
                withContext(Dispatchers.IO) { store.list(CREATED_PREFIX, delimiter = "/").commonPrefixes }
                    .filter { dayPrefix ->
                        val day = parseCreatedDay(dayPrefix)
                        day != null && day <= lastDay
                    }

            dayPrefixes
                .map { dayPrefix -> async(Dispatchers.IO) { store.list(dayPrefix).objectNames } }
                .awaitAll()
                .flatten()
                .map { key -> async(Dispatchers.IO) { store.read<CreatedMarkerDocument>(key) } }
                .awaitAll()
                .filterNotNull()
                .filter { it.createdTimestamp <= createdBefore }
                .sortedBy { it.createdTimestamp }
        }

    private suspend fun indexTestRun(
        publicId: PublicId,
        testRun: TestRunDocument,
    ) {
        withContext(Dispatchers.IO) {
            store.write(
                createdMarkerKey(publicId, testRun.summary.createdTimestamp),
                CreatedMarkerDocument(publicId.id, testRun.summary.createdTimestamp),
            )

            testRun.gitMetadata?.let { git ->
                store.create(organizationRepositoryKey(organizationName(git), git.repoName), OrganizationRepositoryDocument(git.repoName))
            }
        }

        testRun.gitMetadata?.let { git -> refreshShardEntry(publicId, git.repoName, testRun.summary.createdTimestamp) }
    }

    /**
     * Returns true when the marker is resolved and can be deleted.
     */
    private suspend fun repairTestRun(marker: IndexRepairDocument): Boolean {
        val publicId = PublicId(marker.publicId)
        val testRun = documentStore.readTestRunIncludingDeleted(publicId)

        return when {
            testRun != null && !testRun.deleted -> {
                indexTestRun(publicId, testRun)
                true
            }
            testRun != null -> {
                // Being deleted: only the timeline entry needs removing, test run cleanup retries the rest of the delete
                testRun.gitMetadata?.let { git -> refreshShardEntry(publicId, git.repoName, testRun.summary.createdTimestamp) }
                true
            }
            // The test run doesn't exist. If the marker was written before saving it, wait in case it's still being
            // saved. Then remove anything left of it.
            marker.savingTestRun && marker.markedAt.isAfter(Instant.now().minus(MISSING_TEST_RUN_REPAIR_DELAY)) -> false
            else -> {
                if (marker.createdTimestamp != null) {
                    marker.repoName?.let { repoName -> refreshShardEntry(publicId, repoName, marker.createdTimestamp) }
                    removeCreatedMarker(publicId, marker.createdTimestamp)
                }
                true
            }
        }
    }

    private suspend fun markForRepairAfterFailure(
        publicId: PublicId,
        testRun: TestRunDocument?,
        cause: Exception,
    ) {
        logger.error("Failed to update the index for test run $publicId, marking it for repair", cause)

        try {
            writeRepairMarker(publicId, testRun, savingTestRun = false)
        } catch (e: Exception) {
            logger.error("Failed to mark test run $publicId for index repair, its index entries may be out of date", e)
        }
    }

    private suspend fun refreshShardEntry(
        publicId: PublicId,
        repoName: String,
        createdTimestamp: Instant,
    ) {
        val refresh = PendingRefresh(timelineShardKey(repoName, createdTimestamp), repoName, publicId)
        val batcher = shardBatchers[Math.floorMod(refresh.shardKey.hashCode(), SHARD_BATCHER_STRIPES)]

        batcher.pending.add(refresh)

        batcher.mutex.withLock {
            // A refresh that queued while the previous batch was running applies every refresh queued since,
            // including this one, so it may already be done
            if (!refresh.result.isCompleted) {
                applyRefreshes(generateSequence { batcher.pending.poll() }.toList())
            }
        }

        refresh.result.await()
    }

    private suspend fun applyRefreshes(refreshes: List<PendingRefresh>) {
        try {
            for ((shardKey, shardRefreshes) in refreshes.groupBy { it.shardKey }) {
                try {
                    val failures =
                        withContext(Dispatchers.IO) {
                            updateShard(shardKey, shardRefreshes.first().repoName, shardRefreshes.map { it.publicId }.distinct())
                        }

                    shardRefreshes.forEach { refresh ->
                        failures[refresh.publicId]?.let { refresh.result.completeExceptionally(it) } ?: refresh.result.complete(Unit)
                    }
                } catch (e: Exception) {
                    // The other refreshes in the batch weren't cancelled, so they get an ordinary failure
                    val failure = if (e is CancellationException) IllegalStateException("Timeline shard update was cancelled", e) else e
                    shardRefreshes.forEach { it.result.completeExceptionally(failure) }
                }
            }
        } finally {
            // Never leave a queued refresh waiting, whatever happened above
            refreshes.forEach { it.result.completeExceptionally(IllegalStateException("Timeline refresh for ${it.publicId} didn't run")) }
        }
    }

    /**
     * Rebuilds the entries of the given test runs in one compare-and-swap update of the shard.
     * Returns the test runs whose entries couldn't be built, which are left unchanged.
     */
    private fun updateShard(
        shardKey: String,
        repoName: String,
        publicIds: List<PublicId>,
    ): Map<PublicId, Throwable> {
        var failures = mapOf<PublicId, Throwable>()

        store.update<TimelineShardDocument>(shardKey) { current ->
            val shard = current ?: TimelineShardDocument(repoName, listOf())

            // Read the source documents inside the update, after reading the shard, so a concurrent change
            // that commits first is always included when this update commits
            val latestEntries = buildEntries(publicIds)
            failures = latestEntries.mapNotNull { (publicId, result) -> result.exceptionOrNull()?.let { publicId to it } }.toMap()

            val updatedEntries =
                latestEntries.entries.fold(shard.entries) { entries, (publicId, result) ->
                    result.fold({ latestEntry -> withEntry(entries, publicId, latestEntry) }, { entries })
                }

            if (updatedEntries == shard.entries) null else shard.copy(entries = updatedEntries)
        }

        return failures
    }

    // Blocking, since it's called inside the shard's update. Keeps the given order.
    private fun buildEntries(publicIds: List<PublicId>): Map<PublicId, Result<TestRunIndexEntry?>> =
        if (publicIds.size == 1) {
            mapOf(publicIds.first() to runCatching { buildEntry(publicIds.first()) })
        } else {
            publicIds
                .map { publicId -> publicId to entryBuildExecutor.submit(Callable { runCatching { buildEntry(publicId) } }) }
                .associate { (publicId, future) -> publicId to future.get() }
        }

    // Blocking, since it's called inside the shard's update
    private fun buildEntry(publicId: PublicId): TestRunIndexEntry? {
        val testRun = documentStore.readTestRunBlocking(publicId) ?: return null
        val git = testRun.gitMetadata ?: return null

        val coverageRun = store.read<CoverageRunDocument>(ObjectStoreKeys.coverageRun(publicId))
        val performanceResults = store.read<PerformanceResultsDocument>(ObjectStoreKeys.performanceResults(publicId))

        return TestRunIndexEntry(
            publicId = publicId.id,
            createdTimestamp = testRun.summary.createdTimestamp,
            projectName = git.projectName,
            branchName = git.branchName,
            isMainBranch = git.isMainBranch,
            pullRequestNumber = git.pullRequestNumber,
            commitSha = git.commitSha,
            ci = testRun.resultsMetadata?.ci,
            group = testRun.resultsMetadata?.group,
            summary = testRun.summary,
            hasCoverageRun = coverageRun != null,
            coverageStats = coverageRun?.groups?.takeIf { it.isNotEmpty() }?.let { sumCoverageStats(it) },
            performanceResults = performanceResults?.results.orEmpty(),
        )
    }

    private fun parseCreatedDay(dayPrefix: String): LocalDate? =
        try {
            LocalDate.parse(dayPrefix.removePrefix(CREATED_PREFIX).removeSuffix("/"))
        } catch (e: DateTimeParseException) {
            logger.warn("Ignoring unexpected key prefix $dayPrefix in the created date index")
            null
        }

    private class PendingRefresh(val shardKey: String, val repoName: String, val publicId: PublicId) {
        val result = CompletableDeferred<Unit>()
    }

    private class ShardBatcher {
        val mutex = Mutex()
        val pending = ConcurrentLinkedQueue<PendingRefresh>()
    }

    companion object {
        private const val REPOSITORIES_PREFIX = "repos/"
        private const val ORGANIZATIONS_PREFIX = "orgs/"
        private const val CREATED_PREFIX = "created/"
        private const val INDEX_REPAIR_PREFIX = "index-repair/"

        // Refreshes are batched per shard; shards share a stripe's lock, which bounds memory
        private const val SHARD_BATCHER_STRIPES = 64

        // How long a repair marker for a test run that doesn't exist is kept, in case the test run is still being saved
        internal val MISSING_TEST_RUN_REPAIR_DELAY: Duration = Duration.ofMinutes(10)

        // Builds a batch's entries in parallel inside the shard update, keeping the compare-and-swap window short
        private val entryBuildExecutor =
            Executors.newFixedThreadPool(16) { runnable ->
                Thread(runnable, "timeline-entry-builder").apply { isDaemon = true }
            }

        private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZoneOffset.UTC)

        fun repositoryPrefix(repoName: String) = "$REPOSITORIES_PREFIX${encodeKeySegment(repoName)}/"

        private fun timelineShardPrefix(repoName: String) = "${repositoryPrefix(repoName)}timeline/"

        private fun timelineShardKey(
            repoName: String,
            createdTimestamp: Instant,
        ) = "${timelineShardPrefix(repoName)}${monthFormatter.format(createdTimestamp)}.json"

        private fun organizationPrefix(orgName: String) = "$ORGANIZATIONS_PREFIX${encodeKeySegment(orgName)}/"

        private fun organizationRepositoryKey(
            orgName: String,
            repoName: String,
        ) = "${organizationPrefix(orgName)}${encodeKeySegment(repoName)}.json"

        internal fun createdMarkerKey(
            publicId: PublicId,
            createdTimestamp: Instant,
        ) = "$CREATED_PREFIX${LocalDate.ofInstant(createdTimestamp, ZoneOffset.UTC)}/${publicId.id}.json"

        internal fun indexRepairPrefix() = INDEX_REPAIR_PREFIX

        // Matches the database, which stores the organization as the first segment of the repo name
        private fun organizationName(git: GitMetadataDocument) = git.orgName ?: git.repoName.split("/").first()

        /**
         * Repository and project names can contain any characters, including slashes, so they're encoded to
         * keep each one a single key segment.
         */
        fun encodeKeySegment(value: String): String = URLEncoder.encode(value, Charsets.UTF_8)

        private fun decodeKeySegment(value: String): String = URLDecoder.decode(value, Charsets.UTF_8)

        /**
         * Key segment for an optional project name. Distinguishes no project from any project name.
         */
        fun projectKeySegment(projectName: String?) = if (projectName == null) "no-project" else "project-${encodeKeySegment(projectName)}"

        private fun withEntry(
            entries: List<TestRunIndexEntry>,
            publicId: PublicId,
            latestEntry: TestRunIndexEntry?,
        ): List<TestRunIndexEntry> {
            val existingEntry = entries.find { it.publicId == publicId.id }

            return when {
                latestEntry == null && existingEntry == null -> entries
                latestEntry == null -> entries.filter { it.publicId != publicId.id }
                existingEntry == null -> insertInOrder(entries, latestEntry)
                latestEntry == existingEntry -> entries
                else -> entries.map { if (it.publicId == publicId.id) latestEntry else it }
            }
        }

        private fun insertInOrder(
            entries: List<TestRunIndexEntry>,
            newEntry: TestRunIndexEntry,
        ): List<TestRunIndexEntry> {
            val insertIdx = entries.indexOfLast { it.createdTimestamp <= newEntry.createdTimestamp } + 1
            return entries.subList(0, insertIdx) + newEntry + entries.subList(insertIdx, entries.size)
        }

        fun sumCoverageStats(groups: List<CoverageGroupDocument>) =
            CoverageStatsDocument(
                statementCovered = groups.sumOf { it.stats.statementCovered },
                statementMissed = groups.sumOf { it.stats.statementMissed },
                lineCovered = groups.sumOf { it.stats.lineCovered },
                lineMissed = groups.sumOf { it.stats.lineMissed },
                branchCovered = groups.sumOf { it.stats.branchCovered },
                branchMissed = groups.sumOf { it.stats.branchMissed },
            )
    }
}

data class TimelineShardDocument(val repoName: String, val entries: List<TestRunIndexEntry>)

data class TestRunIndexEntry(
    val publicId: String,
    val createdTimestamp: Instant,
    val projectName: String?,
    val branchName: String?,
    val isMainBranch: Boolean,
    val pullRequestNumber: Int?,
    val commitSha: String?,
    // Null when the test run doesn't have results metadata
    val ci: Boolean?,
    val group: String?,
    val summary: TestRunSummaryDocument,
    // Whether coverage was started for the run, even if no coverage groups were saved
    val hasCoverageRun: Boolean,
    // Summed coverage group stats, null when the run doesn't have any coverage groups
    val coverageStats: CoverageStatsDocument?,
    val performanceResults: List<PerformanceResultDocument>,
) {
    fun matchesProject(projectName: String?) = this.projectName == projectName

    fun matchesBranchName(branchName: String?) = branchName == null || this.branchName == branchName

    val isCI: Boolean
        get() = ci == true
}

/**
 * Most recent first. Like the database ordering by created timestamp and then ID descending,
 * runs created at the same time are ordered most recently saved first.
 * Relies on this list being in the order the runs were saved, which the stable sort then preserves in reverse.
 */
fun List<TestRunIndexEntry>.newestFirst(): List<TestRunIndexEntry> = asReversed().sortedByDescending { it.createdTimestamp }

data class CreatedMarkerDocument(val publicId: String, val createdTimestamp: Instant)

data class OrganizationRepositoryDocument(val repoName: String)

/**
 * Marks a test run whose index entries may need rebuilding. Records where the test run's timeline entry is,
 * so the entry can still be found and removed if the test run itself is gone.
 */
data class IndexRepairDocument(
    val publicId: String,
    val repoName: String?,
    val createdTimestamp: Instant?,
    val markedAt: Instant,
    // Written before saving the test run rather than after a failed index update, so the test run may not exist yet
    val savingTestRun: Boolean = false,
)
