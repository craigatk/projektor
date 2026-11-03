package projektor.testrun

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import projektor.incomingresults.mapper.toTestRunSummary
import projektor.incomingresults.mapper.toTestRunSummaryFromApi
import projektor.incomingresults.model.GitMetadata
import projektor.incomingresults.model.GroupedResults
import projektor.incomingresults.model.GroupedTestSuites
import projektor.persistence.objectstore.AttachmentsDocument
import projektor.persistence.objectstore.GitMetadataDocument
import projektor.persistence.objectstore.ObjectStoreKeys
import projektor.persistence.objectstore.ResultsMetadataDocument
import projektor.persistence.objectstore.ResultsProcessingDocument
import projektor.persistence.objectstore.SystemAttributesDocument
import projektor.persistence.objectstore.TestRunDocument
import projektor.persistence.objectstore.TestRunDocumentStore
import projektor.persistence.objectstore.TestRunIndex
import projektor.persistence.objectstore.TestSuiteDocument
import projektor.persistence.objectstore.TestSuiteGroupDocument
import projektor.persistence.objectstore.mapParallelSkippingFailures
import projektor.persistence.objectstore.toApi
import projektor.persistence.objectstore.toDocument
import projektor.persistence.objectstore.toNestedApi
import projektor.persistence.objectstore.toStoredScale
import projektor.server.api.PublicId
import projektor.server.api.TestRun
import projektor.server.api.TestRunSummary
import projektor.server.api.results.ResultsProcessingStatus
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import projektor.parser.model.TestSuite as ParsedTestSuite

class TestRunObjectStoreRepository(
    private val documentStore: TestRunDocumentStore,
    private val testRunIndex: TestRunIndex,
    private val abandonedGroupClaimAge: Duration = TestRunRepository.ABANDONED_GROUP_CLAIM_AGE,
    private val orphanedDataMinAge: Duration = Duration.ofDays(1),
) : TestRunRepository {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    private val store = documentStore.store

    override suspend fun saveTestRun(
        publicId: PublicId,
        testSuites: List<ParsedTestSuite>,
    ): TestRunSummary {
        val testRunSummary = toTestRunSummary(publicId, testSuites, null, null)

        val repairMarkerKey = testRunIndex.writeRepairMarker(publicId, null, savingTestRun = true)

        val testSuiteDocuments = documentStore.writeTestSuites(publicId, testSuites, null, 1)

        createTestRunDocument(
            publicId,
            repairMarkerKey,
            TestRunDocument(
                summary = testRunSummary.toDocument(),
                resultsMetadata = null,
                gitMetadata = null,
                testSuiteGroups = listOf(),
                testSuites = testSuiteDocuments,
                lastReservedTestSuiteIdx = testSuiteDocuments.size,
            ),
        )

        logger.info("Finished saving test run $publicId")

        return testRunSummary
    }

    override suspend fun saveGroupedTestRun(
        publicId: PublicId,
        groupedResults: GroupedResults,
    ): TestRunSummary {
        val testSuites = groupedResults.groupedTestSuites.flatMap { it.testSuites }
        val testRunSummary =
            toTestRunSummary(publicId, testSuites, groupedResults.wallClockDuration, groupedResults.metadata?.createdTimestamp)

        logger.info("Starting saving test run $publicId")

        val repairMarkerKey = testRunIndex.writeRepairMarker(publicId, null, savingTestRun = true)

        val testSuiteGroupDocuments = mutableListOf<TestSuiteGroupDocument>()
        val testSuiteDocuments = mutableListOf<TestSuiteDocument>()

        groupedResults.groupedTestSuites.forEach { groupedTestSuites ->
            val testSuiteGroupDocument = groupedTestSuites.toDocument()
            testSuiteGroupDocuments.add(testSuiteGroupDocument)

            testSuiteDocuments +=
                documentStore.writeTestSuites(publicId, groupedTestSuites.testSuites, testSuiteGroupDocument, testSuiteDocuments.size + 1)
        }

        createTestRunDocument(
            publicId,
            repairMarkerKey,
            TestRunDocument(
                summary = testRunSummary.toDocument(),
                resultsMetadata = groupedResults.metadata?.let { ResultsMetadataDocument(it.ci, it.group) },
                gitMetadata = groupedResults.metadata?.git?.toDocument(),
                testSuiteGroups = testSuiteGroupDocuments,
                testSuites = testSuiteDocuments,
                lastReservedTestSuiteIdx = testSuiteDocuments.size,
            ),
        )

        logger.info("Finished saving test run $publicId")

        return testRunSummary
    }

    /**
     * Like the database repository, the appended test suites are numbered from startingIdx + 1.
     * Unlike the database, concurrent appends to the same test run get distinct test suite indexes:
     * if another append already reserved those indexes, the next free indexes are used instead.
     */
    override suspend fun appendTestSuites(
        publicId: PublicId,
        startingIdx: Int,
        groupedTestSuites: List<GroupedTestSuites>,
    ) {
        logger.info("Starting appending test suites to $publicId")

        val testSuiteCount = groupedTestSuites.sumOf { it.testSuites.size }
        var firstTestSuiteIdx = startingIdx + 1

        val testRun =
            updateTestRunDocument(publicId) { current ->
                firstTestSuiteIdx = maxOf(startingIdx + 1, current.lastReservedTestSuiteIdx + 1)
                current.copy(lastReservedTestSuiteIdx = firstTestSuiteIdx + testSuiteCount - 1)
            }

        val newTestSuiteGroupDocuments = mutableListOf<TestSuiteGroupDocument>()
        val newTestSuiteDocuments = mutableListOf<TestSuiteDocument>()
        var testSuiteStartingIndex = firstTestSuiteIdx

        groupedTestSuites.forEach { groupedTestSuites ->
            val testSuiteGroupDocument =
                findTestSuiteGroup(testRun.testSuiteGroups + newTestSuiteGroupDocuments, groupedTestSuites.groupName)
                    ?: groupedTestSuites.toDocument().also { newTestSuiteGroupDocuments.add(it) }

            newTestSuiteDocuments +=
                documentStore.writeTestSuites(publicId, groupedTestSuites.testSuites, testSuiteGroupDocument, testSuiteStartingIndex)

            testSuiteStartingIndex += groupedTestSuites.testSuites.size
        }

        updateTestRunDocument(publicId) { current ->
            val groupsToAdd = newTestSuiteGroupDocuments.filter { findTestSuiteGroup(current.testSuiteGroups, it.groupName) == null }
            // These indexes were reserved for this append, so any already there are from this update running twice
            val newIndexes = newTestSuiteDocuments.map { it.idx }.toSet()

            current.copy(
                testSuiteGroups = current.testSuiteGroups + groupsToAdd,
                testSuites = (current.testSuites.filter { it.idx !in newIndexes } + newTestSuiteDocuments).sortedBy { it.idx },
            )
        }
    }

    override suspend fun updateTestRunSummary(
        publicId: PublicId,
        wallClockDuration: BigDecimal?,
    ): TestRunSummary {
        lateinit var testRunSummary: TestRunSummary

        val testCaseChunks =
            documentStore.readTestRun(publicId)?.let { documentStore.readTestCaseChunks(publicId, it) }.orEmpty().toMutableMap()

        updateTestRunDocument(publicId) { current ->
            val updatedWallClockDuration: BigDecimal =
                (current.summary.wallClockDuration ?: BigDecimal.ZERO) + (wallClockDuration ?: BigDecimal.ZERO)

            // Calculated from the test suites in the version of the document being replaced, so an append that
            // commits in between makes this update retry with that append's test suites included
            val testCasesBySuite = documentStore.readAllTestCasesBlocking(publicId, current, testCaseChunks)
            val testSuites =
                current.testSuites.map { testSuite ->
                    testSuite.toApi(testCasesBySuite[testSuite.idx].orEmpty().map { it.toNestedApi(publicId, current, testSuite) })
                }

            // Matches the database repository, which returns the time of the update as the created timestamp
            // but leaves the stored created timestamp unchanged
            testRunSummary = toTestRunSummaryFromApi(publicId, testSuites, updatedWallClockDuration, Instant.now())

            current.copy(
                summary =
                    testRunSummary.toDocument().copy(
                        createdTimestamp = current.summary.createdTimestamp,
                        wallClockDuration = updatedWallClockDuration.toStoredScale(),
                    ),
            )
        }

        testRunIndex.refreshEntry(publicId)

        return testRunSummary
    }

    override suspend fun fetchTestRun(publicId: PublicId): TestRun? =
        documentStore.readTestRun(publicId)?.let { testRun ->
            TestRun(
                id = publicId.id,
                summary = testRun.summary.toApi(publicId),
                testSuites = testRun.testSuites.map { it.toApi(null) },
            )
        }

    override suspend fun fetchTestRunSummary(publicId: PublicId): TestRunSummary? =
        documentStore.readTestRun(publicId)?.summary?.toApi(publicId)

    /**
     * Every step can be retried, so test run cleanup finishes a delete that failed partway:
     * 1. Mark the test run document deleted, so the test run reads as missing and leaves the indexes.
     * 2. Remove its timeline entry and release its group claim.
     * 3. Delete its documents, then the test run document. Cleanup retries from step 1 as long as the test run document
     *    exists, so it goes last.
     * 4. Remove its created date marker. If the delete fails before this, cleanup finds the marker without a test
     *    run and removes it (see findTestRunsCreatedBeforeAndNotPinned).
     *
     * Also removes the attachments document if it's empty, which is all that's left once the attachments are deleted.
     * This has to happen even without a test run, for cleaning up data found by [findOrphanedTestRunData].
     */
    override suspend fun deleteTestRun(publicId: PublicId) {
        val deletedTestRun =
            // Returns the document as already marked deleted when retrying a delete
            documentStore.updateTestRun(publicId) { current ->
                current?.takeUnless { it.deleted }?.copy(deleted = true)
            }

        if (deletedTestRun == null) {
            withContext(Dispatchers.IO) { deleteEmptyAttachmentsDocument(publicId) }
            return
        }

        testRunIndex.removeTestRun(publicId, deletedTestRun)

        val group = deletedTestRun.resultsMetadata?.group
        val repoName = deletedTestRun.gitMetadata?.repoName
        if (group != null && repoName != null) {
            withContext(Dispatchers.IO) {
                releaseGroupClaim(repoName, group, publicId)
            }
        }

        withContext(Dispatchers.IO) {
            val testRunKey = ObjectStoreKeys.testRun(publicId)

            store.deleteAll(store.list(ObjectStoreKeys.testRunPrefix(publicId)).objectNames - testRunKey)
            deleteEmptyAttachmentsDocument(publicId)
            store.delete(testRunKey)
        }

        testRunIndex.removeCreatedMarker(publicId, deletedTestRun.summary.createdTimestamp)
    }

    override suspend fun findTestRunsCreatedBeforeAndNotPinned(createdBefore: LocalDate): List<PublicId> =
        findTestRunsCreatedBeforeAndNotPinned(createdBefore, withAttachmentsOnly = false)

    override suspend fun findTestRunsCreatedBeforeAndNotPinnedWithAttachments(createdBefore: LocalDate): List<PublicId> =
        findTestRunsCreatedBeforeAndNotPinned(createdBefore, withAttachmentsOnly = true)

    override suspend fun findOrphanedTestRunData(): List<PublicId> {
        // Past the last day of the created date index, so this reads none of the markers
        val createdPublicIds =
            testRunIndex.readTestRunsCreatedAtOrBefore(Instant.now().plus(Duration.ofDays(2))).map { it.publicId.id }.toSet()

        val coverageKeys =
            withContext(Dispatchers.IO) { store.list(ObjectStoreKeys.COVERAGE_PREFIX, delimiter = "/").commonPrefixes }
                .map { prefix -> PublicId(prefix.removePrefix(ObjectStoreKeys.COVERAGE_PREFIX).removeSuffix("/")) }
                .map { publicId -> publicId to ObjectStoreKeys.coverageRun(publicId) }
        val attachmentKeys =
            withContext(Dispatchers.IO) { store.list(ObjectStoreKeys.ATTACHMENTS_PREFIX).objectNames }
                .map { key -> PublicId(key.removePrefix(ObjectStoreKeys.ATTACHMENTS_PREFIX).removeSuffix(".json")) to key }

        val oldestAllowedWrite = Instant.now().minus(orphanedDataMinAge)

        return (coverageKeys + attachmentKeys)
            .filter { (publicId, _) -> publicId.id !in createdPublicIds }
            .groupBy({ it.first }, { it.second })
            .toList()
            .mapParallelSkippingFailures(logger) { (publicId, keys) ->
                // Coverage and attachments can be published before their test run finishes saving
                val recentlyWritten = keys.any { key -> store.lastModified(key)?.isAfter(oldestAllowedWrite) == true }

                publicId.takeIf { !recentlyWritten && !store.exists(ObjectStoreKeys.testRun(publicId)) }
            }
    }

    override suspend fun claimTestRunGroup(
        group: String,
        repoName: String,
        publicId: PublicId,
    ): PublicId {
        val claimKey = groupClaimKey(repoName, group)

        repeat(MAX_CLAIM_ATTEMPTS) {
            val newClaim = GroupClaimDocument(publicId.id, Instant.now())

            if (withContext(Dispatchers.IO) { store.create(claimKey, newClaim) } != null) {
                return publicId
            }

            // Null when the claim was released since the create, so try to claim it again
            val existingClaim = withContext(Dispatchers.IO) { store.readVersioned(claimKey, GroupClaimDocument::class.java) }

            if (existingClaim != null) {
                if (!isAbandoned(existingClaim.value)) {
                    return PublicId(existingClaim.value.testRunPublicId)
                }

                // Only replaces the claim if nobody else took it over first, otherwise reads the new claim
                if (withContext(Dispatchers.IO) { store.replace(claimKey, newClaim, existingClaim.etag) } != null) {
                    logger.warn(
                        "Test run $publicId took over results group $group in $repoName from abandoned test run " +
                            existingClaim.value.testRunPublicId,
                    )
                    return publicId
                }
            }
        }

        throw IllegalStateException("Couldn't claim results group $group in $repoName after $MAX_CLAIM_ATTEMPTS attempts")
    }

    private suspend fun isAbandoned(claim: GroupClaimDocument): Boolean {
        val owner = PublicId(claim.testRunPublicId)

        if (documentStore.readTestRun(owner) != null) {
            return false
        }

        val ownerFailed =
            withContext(Dispatchers.IO) {
                store.read<ResultsProcessingDocument>(ObjectStoreKeys.resultsProcessing(owner))
            }?.status == ResultsProcessingStatus.ERROR.name
        // Claims written before claims had a timestamp count as old
        val claimedAt = claim.claimedAt ?: Instant.EPOCH

        return ownerFailed || claimedAt.isBefore(Instant.now().minus(abandonedGroupClaimAge))
    }

    override suspend fun releaseTestRunGroupClaim(
        group: String,
        repoName: String,
        publicId: PublicId,
    ) {
        withContext(Dispatchers.IO) {
            releaseGroupClaim(repoName, group, publicId)
        }
    }

    override suspend fun findTestRunWithGroup(
        group: String,
        repoName: String,
    ): PublicId? =
        // The database query has no order, in practice returning the first saved run with the group
        testRunIndex.readRepositoryEntries(repoName)
            .firstOrNull { it.group == group }
            ?.let { PublicId(it.publicId) }

    /**
     * Cleanup runs this over every test run past its age limit each day, including those it keeps (such as when
     * attachments have a shorter limit than test runs), so it avoids reading each test run's document. When looking
     * for attachments, listing the attachment documents rules out most test runs without a request for each.
     */
    private suspend fun findTestRunsCreatedBeforeAndNotPinned(
        createdBefore: LocalDate,
        withAttachmentsOnly: Boolean,
    ): List<PublicId> {
        val createdBeforeTimestamp = createdBefore.atStartOfDay().toInstant(ZoneOffset.UTC)
        val createdTestRuns = testRunIndex.readTestRunsCreatedAtOrBefore(createdBeforeTimestamp)

        val candidates =
            if (withAttachmentsOnly) {
                val attachmentKeys =
                    withContext(Dispatchers.IO) { store.list(ObjectStoreKeys.ATTACHMENTS_PREFIX).objectNames }.toSet()

                createdTestRuns.filter { ObjectStoreKeys.attachments(it.publicId) in attachmentKeys }
            } else {
                createdTestRuns
            }

        return candidates
            .mapParallelSkippingFailures(logger) { createdTestRun ->
                val publicId = createdTestRun.publicId

                if (withAttachmentsOnly &&
                    store.read<AttachmentsDocument>(ObjectStoreKeys.attachments(publicId))?.attachments.orEmpty().isEmpty()
                ) {
                    return@mapParallelSkippingFailures null
                }

                // Includes test runs marked deleted, so cleanup retries a delete that failed partway
                if (!store.exists(ObjectStoreKeys.testRun(publicId))) {
                    // Left behind by a delete that removed the test run but failed before removing the marker
                    testRunIndex.removeCreatedMarker(createdTestRun)
                    return@mapParallelSkippingFailures null
                }

                val pinned = store.read<SystemAttributesDocument>(ObjectStoreKeys.systemAttributes(publicId))?.pinned ?: false

                publicId.takeIf { !pinned }
            }
    }

    /**
     * Saves the test run document and indexes it. The repair marker written before the test run's first document covers
     * a crash between the two; if indexing fails, the test run is still saved and the index repair job finishes indexing
     * it. The marker stays if the test run already exists, so that test run gets indexed whatever wrote it.
     */
    private suspend fun createTestRunDocument(
        publicId: PublicId,
        repairMarkerKey: String,
        testRunDocument: TestRunDocument,
    ) {
        val created = withContext(Dispatchers.IO) { store.create(ObjectStoreKeys.testRun(publicId), testRunDocument) }

        if (created == null) {
            throw IllegalStateException("Test run $publicId already exists")
        }

        if (testRunIndex.addTestRun(publicId, testRunDocument)) {
            testRunIndex.clearRepairMarker(repairMarkerKey)
        }
    }

    private suspend fun updateTestRunDocument(
        publicId: PublicId,
        transform: (TestRunDocument) -> TestRunDocument,
    ): TestRunDocument =
        documentStore.updateTestRun(publicId) { current ->
            transform(current?.takeUnless { it.deleted } ?: throw IllegalStateException("Test run $publicId does not exist"))
        }!!

    // A claim is only ever written when the group has no claim, so a claim owned by this test run can't change
    // between reading and deleting it
    private fun releaseGroupClaim(
        repoName: String,
        group: String,
        publicId: PublicId,
    ) {
        val claimKey = groupClaimKey(repoName, group)
        if (store.read<GroupClaimDocument>(claimKey)?.testRunPublicId == publicId.id) {
            store.delete(claimKey)
        }
    }

    private fun deleteEmptyAttachmentsDocument(publicId: PublicId) {
        val key = ObjectStoreKeys.attachments(publicId)

        if (store.read<AttachmentsDocument>(key)?.attachments?.isEmpty() == true) {
            store.delete(key)
        }
    }

    data class GroupClaimDocument(val testRunPublicId: String, val claimedAt: Instant? = null)

    companion object {
        private const val MAX_CLAIM_ATTEMPTS = 10

        private fun groupClaimKey(
            repoName: String,
            group: String,
        ) = "${TestRunIndex.repositoryPrefix(repoName)}group-claims/${TestRunIndex.encodeKeySegment(group)}.json"

        // Like the database's group_name = ? lookup, a group without a name never matches an existing group
        private fun findTestSuiteGroup(
            testSuiteGroups: List<TestSuiteGroupDocument>,
            groupName: String?,
        ): TestSuiteGroupDocument? = groupName?.let { testSuiteGroups.find { group -> group.groupName == groupName } }

        private fun GroupedTestSuites.toDocument() = TestSuiteGroupDocument(groupName, groupLabel, directory)

        private fun GitMetadata.toDocument(): GitMetadataDocument? =
            repoName?.let {
                GitMetadataDocument(
                    repoName = it,
                    orgName = it.split("/").firstOrNull(),
                    projectName = projectName,
                    branchName = branchName,
                    isMainBranch = isMainBranch,
                    // Saved with the test run so they're available as soon as it is. A pull request number found when
                    // commenting on the pull request is added afterwards (see GroupedTestResultsService).
                    pullRequestNumber = pullRequestNumber,
                    commitSha = commitSha,
                )
            }
    }
}
