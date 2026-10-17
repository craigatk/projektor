package projektor.testrun

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import projektor.incomingresults.mapper.toTestRunSummary
import projektor.incomingresults.mapper.toTestRunSummaryFromApi
import projektor.incomingresults.model.GitMetadata
import projektor.incomingresults.model.GroupedResults
import projektor.incomingresults.model.GroupedTestSuites
import projektor.persistence.objectstore.GitMetadataDocument
import projektor.persistence.objectstore.ObjectStoreKeys
import projektor.persistence.objectstore.ResultsMetadataDocument
import projektor.persistence.objectstore.TestRunDocument
import projektor.persistence.objectstore.TestRunDocumentStore
import projektor.persistence.objectstore.TestSuiteDocument
import projektor.persistence.objectstore.TestSuiteGroupDocument
import projektor.persistence.objectstore.toApi
import projektor.persistence.objectstore.toDocument
import projektor.persistence.objectstore.toStoredScale
import projektor.server.api.PublicId
import projektor.server.api.TestRun
import projektor.server.api.TestRunSummary
import projektor.server.api.TestSuite
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import projektor.parser.model.TestSuite as ParsedTestSuite

class TestRunObjectStoreRepository(private val documentStore: TestRunDocumentStore) : TestRunRepository {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    private val store = documentStore.store

    override suspend fun saveTestRun(
        publicId: PublicId,
        testSuites: List<ParsedTestSuite>,
    ): TestRunSummary {
        val testRunSummary = toTestRunSummary(publicId, testSuites, null, null)

        val testSuiteDocuments = documentStore.writeTestSuites(publicId, testSuites, null, 1)

        createTestRunDocument(
            publicId,
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

            current.copy(
                testSuiteGroups = current.testSuiteGroups + groupsToAdd,
                testSuites = (current.testSuites + newTestSuiteDocuments).sortedBy { it.idx },
            )
        }
    }

    override suspend fun updateTestRunSummary(
        publicId: PublicId,
        testSuites: List<TestSuite>,
        wallClockDuration: BigDecimal?,
    ): TestRunSummary {
        lateinit var testRunSummary: TestRunSummary

        updateTestRunDocument(publicId) { current ->
            val updatedWallClockDuration: BigDecimal =
                (current.summary.wallClockDuration ?: BigDecimal.ZERO) + (wallClockDuration ?: BigDecimal.ZERO)

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

    override suspend fun deleteTestRun(publicId: PublicId) {
        withContext(Dispatchers.IO) {
            store.deletePrefix(ObjectStoreKeys.testRunPrefix(publicId))
        }
    }

    override suspend fun findTestRunsCreatedBeforeAndNotPinned(createdBefore: LocalDate): List<PublicId> =
        TODO("Requires the created-date index from phase 3 of the object-store persistence plan")

    override suspend fun findTestRunsCreatedBeforeAndNotPinnedWithAttachments(createdBefore: LocalDate): List<PublicId> =
        TODO("Requires the created-date index from phase 3 of the object-store persistence plan")

    override suspend fun findTestRunWithGroup(
        group: String,
        repoName: String,
    ): PublicId? = TODO("Requires the group index from phase 3 of the object-store persistence plan")

    private suspend fun createTestRunDocument(
        publicId: PublicId,
        testRunDocument: TestRunDocument,
    ) {
        withContext(Dispatchers.IO) {
            store.create(ObjectStoreKeys.testRun(publicId), testRunDocument)
                ?: throw IllegalStateException("Test run $publicId already exists")
        }
    }

    private suspend fun updateTestRunDocument(
        publicId: PublicId,
        transform: (TestRunDocument) -> TestRunDocument,
    ): TestRunDocument =
        withContext(Dispatchers.IO) {
            store.update<TestRunDocument>(ObjectStoreKeys.testRun(publicId)) { current ->
                transform(current ?: throw IllegalStateException("Test run $publicId does not exist"))
            }!!
        }

    companion object {
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
                    // Like the database repository, these are only set by updateGitMetadata after the test run is saved
                    pullRequestNumber = null,
                    commitSha = null,
                )
            }
    }
}
