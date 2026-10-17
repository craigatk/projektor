package projektor.persistence.objectstore

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import projektor.objectstore.json.JsonObjectStore
import projektor.server.api.PublicId
import java.net.URLDecoder
import java.net.URLEncoder
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

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
 */
class TestRunIndex(private val documentStore: TestRunDocumentStore) {
    private val store: JsonObjectStore = documentStore.store

    /**
     * Adds a newly saved test run to the indexes.
     */
    suspend fun addTestRun(
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

        refreshEntry(publicId, testRun)
    }

    /**
     * Removes a deleted test run from the indexes. Takes the test run document as it was before it was deleted,
     * which locates the run's index entries.
     */
    suspend fun removeTestRun(
        publicId: PublicId,
        deletedTestRun: TestRunDocument,
    ) {
        refreshEntry(publicId, deletedTestRun)

        withContext(Dispatchers.IO) {
            store.delete(createdMarkerKey(publicId, deletedTestRun.summary.createdTimestamp))
        }
    }

    /**
     * Rebuilds the test run's repository timeline entry from its current documents, removing the entry if the
     * test run no longer exists. Does nothing for test runs without Git metadata, which aren't in the timeline.
     *
     * @param knownTestRun the test run document if the caller already has it, which saves reading it to find
     * the entry's shard. Required when the test run has been deleted.
     */
    suspend fun refreshEntry(
        publicId: PublicId,
        knownTestRun: TestRunDocument? = null,
    ) {
        val testRunForShard = knownTestRun ?: documentStore.readTestRun(publicId) ?: return
        val git = testRunForShard.gitMetadata ?: return

        val shardKey = timelineShardKey(git.repoName, testRunForShard.summary.createdTimestamp)

        withContext(Dispatchers.IO) {
            store.update<TimelineShardDocument>(shardKey) { current ->
                val shard = current ?: TimelineShardDocument(git.repoName, listOf())

                // Read the source documents inside the update, after reading the shard, so a concurrent change
                // that commits first is always included when this update commits
                val latestEntry = buildEntry(publicId)
                val existingEntry = shard.entries.find { it.publicId == publicId.id }

                when {
                    latestEntry == null && existingEntry == null -> null
                    latestEntry == null -> shard.copy(entries = shard.entries.filter { it.publicId != publicId.id })
                    existingEntry == null -> shard.copy(entries = insertInOrder(shard.entries, latestEntry))
                    latestEntry == existingEntry -> null
                    else -> shard.copy(entries = shard.entries.map { if (it.publicId == publicId.id) latestEntry else it })
                }
            }
        }
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
                    .filter { dayPrefix -> LocalDate.parse(dayPrefix.removePrefix(CREATED_PREFIX).removeSuffix("/")) <= lastDay }

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

    // Blocking, since it's called inside the shard's update
    private fun buildEntry(publicId: PublicId): TestRunIndexEntry? {
        val testRun = store.read<TestRunDocument>(ObjectStoreKeys.testRun(publicId)) ?: return null
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

    companion object {
        private const val REPOSITORIES_PREFIX = "repos/"
        private const val ORGANIZATIONS_PREFIX = "orgs/"
        private const val CREATED_PREFIX = "created/"

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

        private fun createdMarkerKey(
            publicId: PublicId,
            createdTimestamp: Instant,
        ) = "$CREATED_PREFIX${LocalDate.ofInstant(createdTimestamp, ZoneOffset.UTC)}/${publicId.id}.json"

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
 * Most recent first. Like the database's descending created timestamp order (which has no tiebreaker),
 * runs created at the same time stay in the order they were saved.
 */
fun List<TestRunIndexEntry>.newestFirst(): List<TestRunIndexEntry> = sortedByDescending { it.createdTimestamp }

data class CreatedMarkerDocument(val publicId: String, val createdTimestamp: Instant)

data class OrganizationRepositoryDocument(val repoName: String)
