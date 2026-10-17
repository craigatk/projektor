package projektor.persistence

import kotlinx.coroutines.runBlocking
import projektor.TestSuiteData
import projektor.incomingresults.model.GitMetadata
import projektor.incomingresults.model.GroupedResults
import projektor.incomingresults.model.GroupedTestSuites
import projektor.incomingresults.model.ResultsMetadata
import projektor.parser.coverage.payload.CoverageFilePayload
import projektor.parser.model.Failure
import projektor.parser.model.Skipped
import projektor.server.api.PublicId
import projektor.server.api.attachments.Attachment
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import projektor.parser.model.TestCase as ParsedTestCase
import projektor.parser.model.TestSuite as ParsedTestSuite

/**
 * Creates test data through the repository interfaces, so it works with every persistence backend.
 *
 * Follows the same conventions as TestRunDBGenerator (test case class names are "{name}ClassName", test cases take
 * 2.5 seconds, failures have "{name} failure message/type/text") but saves the data the way real results are saved,
 * so derived values like the test run summary are calculated from the test suites.
 */
class TestRunGenerator(private val repositories: RepositoryTestCase) {
    fun createTestRun(
        publicId: PublicId,
        testSuiteDataList: List<TestSuiteData>,
        git: GitMetadata? = null,
        ci: Boolean? = null,
        group: String? = null,
        createdTimestamp: Instant? = null,
    ): PublicId =
        createGroupedTestRun(publicId, listOf(TestSuiteGroupData(null, null, testSuiteDataList)), git, ci, group, createdTimestamp)

    fun createGroupedTestRun(
        publicId: PublicId,
        testSuiteGroups: List<TestSuiteGroupData>,
        git: GitMetadata? = null,
        ci: Boolean? = null,
        group: String? = null,
        createdTimestamp: Instant? = null,
    ): PublicId {
        val metadata =
            if (git != null || ci != null || group != null || createdTimestamp != null) {
                ResultsMetadata(git, ci, group, createdTimestamp)
            } else {
                null
            }

        val groupedResults =
            GroupedResults(
                groupedTestSuites =
                    testSuiteGroups
                        .filter { it.testSuites.isNotEmpty() || it.groupName != null }
                        .map { GroupedTestSuites(it.testSuites.map(::toParsedTestSuite), it.groupName, it.groupLabel, null) },
                performanceResults = listOf(),
                metadata = metadata,
                wallClockDuration = null,
                coverageFiles = null,
                codeQualityReports = null,
            )

        runBlocking {
            repositories.testRunRepository.saveGroupedTestRun(publicId, groupedResults)

            if (git != null && (git.pullRequestNumber != null || git.commitSha != null)) {
                repositories.testRunMetadataRepository.updateGitMetadata(publicId, git.pullRequestNumber, git.commitSha)
            }
        }

        return publicId
    }

    fun createTestRunInRepo(
        publicId: PublicId,
        testSuiteDataList: List<TestSuiteData>,
        repoName: String,
        ci: Boolean,
        projectName: String?,
        branchName: String = "main",
        pullRequestNumber: Int? = null,
        commitSha: String? = null,
        createdTimestamp: Instant? = null,
    ): PublicId =
        createTestRun(
            publicId,
            testSuiteDataList,
            git = GitMetadata(repoName, projectName, branchName, branchName == "main", commitSha, pullRequestNumber),
            ci = ci,
            createdTimestamp = createdTimestamp,
        )

    fun createSimpleTestRun(publicId: PublicId): PublicId = createTestRun(publicId, listOf(simpleTestSuite()))

    fun createSimpleFailingTestRun(publicId: PublicId): PublicId = createTestRun(publicId, listOf(simpleFailingTestSuite()))

    fun createEmptyTestRun(publicId: PublicId): PublicId = createTestRun(publicId, listOf())

    fun createEmptyTestRunInRepo(
        publicId: PublicId,
        repoName: String,
        ci: Boolean,
        projectName: String?,
    ): PublicId = createTestRunInRepo(publicId, listOf(), repoName, ci, projectName)

    fun createSimpleTestRunInRepo(
        publicId: PublicId,
        repoName: String,
        ci: Boolean,
        projectName: String?,
    ): PublicId = createTestRunInRepo(publicId, listOf(simpleTestSuite()), repoName, ci, projectName)

    fun createSimpleFailingTestRunInRepo(
        publicId: PublicId,
        repoName: String,
        ci: Boolean,
        projectName: String?,
    ): PublicId = createTestRunInRepo(publicId, listOf(simpleFailingTestSuite()), repoName, ci, projectName)

    fun createTestRun(
        publicId: PublicId,
        createdOn: LocalDate,
        pinned: Boolean,
    ): PublicId {
        createTestRun(publicId, listOf(), createdTimestamp = createdOn.atStartOfDay().toInstant(ZoneOffset.UTC))

        runBlocking {
            if (pinned) {
                repositories.testRunSystemAttributesRepository.pin(publicId)
            } else {
                repositories.testRunSystemAttributesRepository.unpin(publicId)
            }
        }

        return publicId
    }

    fun createTestRunWithCoverageAndGitMetadata(
        publicId: PublicId,
        coverageText: String,
        repoName: String,
        branchName: String = "main",
        projectName: String? = null,
        pullRequestNumber: Int? = null,
    ): PublicId =
        createTestRunWithCoverageAndGitMetadata(
            publicId,
            CoverageFilePayload(coverageText),
            repoName,
            branchName,
            projectName,
            pullRequestNumber,
        )

    fun createTestRunWithCoverageAndGitMetadata(
        publicId: PublicId,
        coverageFilePayload: CoverageFilePayload,
        repoName: String,
        branchName: String = "main",
        projectName: String? = null,
        pullRequestNumber: Int? = null,
    ): PublicId {
        createTestRun(
            publicId,
            listOf(simpleTestSuite()),
            git = GitMetadata(repoName, projectName, branchName, branchName == "main", null, pullRequestNumber),
        )

        runBlocking { repositories.coverageService.saveReport(coverageFilePayload, publicId) }

        return publicId
    }

    fun addAttachment(
        publicId: PublicId,
        objectName: String,
        fileName: String,
    ) {
        runBlocking { repositories.attachmentRepository.addAttachment(publicId, Attachment(fileName, objectName, null)) }
    }

    companion object {
        fun simpleTestSuite() = TestSuiteData("testSuite1", listOf("testSuite1TestCase1"), listOf(), listOf())

        fun simpleFailingTestSuite() =
            TestSuiteData("testSuite1", listOf("testSuite1TestCase1"), listOf("failingTestSuite1TestCase1"), listOf())

        fun toParsedTestSuite(testSuiteData: TestSuiteData): ParsedTestSuite {
            val testCases =
                testSuiteData.passingTestCaseNames.map { createTestCase(it) } +
                    testSuiteData.failingTestCaseNames.map { name ->
                        createTestCase(name).apply {
                            failure =
                                Failure().apply {
                                    message = "$name failure message"
                                    type = "$name failure type"
                                    text = "$name failure text"
                                }
                        }
                    } +
                    testSuiteData.skippedTestCaseNames.map { name -> createTestCase(name).apply { skipped = Skipped() } }

            return ParsedTestSuite().apply {
                name = testSuiteData.packageAndClassName
                file = testSuiteData.fileName
                tests = testCases.size
                failures = testSuiteData.failingTestCaseNames.size
                skipped = testSuiteData.skippedTestCaseNames.size
                time = BigDecimal.TEN
                timestamp = LocalDateTime.now()
                hostname = "hostname"
                this.testCases = testCases
            }
        }

        private fun createTestCase(testCaseName: String) =
            ParsedTestCase().apply {
                name = testCaseName
                className = "${testCaseName}ClassName"
                time = BigDecimal("2.5")
            }
    }
}

data class TestSuiteGroupData(val groupName: String?, val groupLabel: String?, val testSuites: List<TestSuiteData>)
