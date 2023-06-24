package projektor

import kotlinx.coroutines.runBlocking
import projektor.coverage.CoverageService
import projektor.database.generated.tables.daos.GitMetadataDao
import projektor.database.generated.tables.daos.ResultsMetadataDao
import projektor.database.generated.tables.daos.TestCaseDao
import projektor.database.generated.tables.daos.TestFailureDao
import projektor.database.generated.tables.daos.TestRunAttachmentDao
import projektor.database.generated.tables.daos.TestRunDao
import projektor.database.generated.tables.daos.TestRunSystemAttributesDao
import projektor.database.generated.tables.daos.TestSuiteDao
import projektor.database.generated.tables.daos.TestSuiteGroupDao
import projektor.database.generated.tables.pojos.TestRunAttachment
import projektor.database.generated.tables.pojos.TestRunSystemAttributes
import projektor.incomingresults.mapper.parsePackageAndClassName
import projektor.parser.coverage.payload.CoverageFilePayload
import projektor.server.api.PublicId
import projektor.server.api.util.calculateAverageDuration
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import projektor.database.generated.tables.pojos.GitMetadata as GitMetadataDB
import projektor.database.generated.tables.pojos.ResultsMetadata as ResultsMetadataDB
import projektor.database.generated.tables.pojos.TestCase as TestCaseDB
import projektor.database.generated.tables.pojos.TestFailure as TestFailureDB
import projektor.database.generated.tables.pojos.TestRun as TestRunDB
import projektor.database.generated.tables.pojos.TestSuite as TestSuiteDB
import projektor.database.generated.tables.pojos.TestSuiteGroup as TestSuiteGroupDB

class TestRunDBGenerator(
    private val testRunDao: TestRunDao,
    private val testSuiteGroupDao: TestSuiteGroupDao,
    private val testSuiteDao: TestSuiteDao,
    private val testCaseDao: TestCaseDao,
    private val testFailureDao: TestFailureDao,
    private val testRunSystemAttributesDao: TestRunSystemAttributesDao,
    private val gitMetadataDao: GitMetadataDao,
    private val resultsMetadataDao: ResultsMetadataDao,
    private val coverageService: CoverageService,
    private val attachmentDao: TestRunAttachmentDao
) {
    fun createTestRun(publicId: PublicId, testSuiteDataList: List<TestSuiteData>): TestRunDB {
        val testRun = createTestRun(publicId, testSuiteDataList.size)
        testRunDao.insert(testRun)
        println("Inserted test run ${testRun.publicId}")

        testSuiteDataList.forEachIndexed { testSuiteIdx, testSuiteData ->
            var testCaseIdx = 1

            val testSuite = createTestSuite(testRun.id, testSuiteData.packageAndClassName, testSuiteIdx + 1)
            testSuite.testCount = testSuiteData.passingTestCaseNames.size + testSuiteData.failingTestCaseNames.size + testSuiteData.skippedTestCaseNames.size
            testSuite.passingCount = testSuiteData.passingTestCaseNames.size
            testSuite.failureCount = testSuiteData.failingTestCaseNames.size
            testSuite.skippedCount = testSuiteData.skippedTestCaseNames.size
            testSuite.fileName = testSuiteData.fileName
            testSuiteDao.insert(testSuite)

            testSuiteData.passingTestCaseNames.forEach { testCaseName ->
                val testCase = createTestCase(testSuite.id, testCaseName, testCaseIdx, true)
                testCaseDao.insert(testCase)
                testCaseIdx += 1
            }

            testSuiteData.failingTestCaseNames.forEach { testCaseName ->
                val testCase = createTestCase(testSuite.id, testCaseName, testCaseIdx, false)
                testCaseDao.insert(testCase)

                val testFailure = createTestFailure(testCase.id, testCaseName)
                testFailureDao.insert(testFailure)

                testCaseIdx += 1
            }

            testSuiteData.skippedTestCaseNames.forEach { skippedTestCaseName ->
                val testCase = createTestCase(testSuite.id, skippedTestCaseName, testCaseIdx, false)
                testCase.skipped = true
                testCaseDao.insert(testCase)

                testCaseIdx += 1
            }
        }

        return testRun
    }

    fun createTestRunInRepo(
        publicId: PublicId,
        testSuiteDataList: List<TestSuiteData>,
        repoName: String,
        ci: Boolean,
        projectName: String?,
        branchName: String = "main"
    ): TestRunDB {
        val testRunDB = createTestRun(publicId, testSuiteDataList)
        addResultsMetadata(testRunDB, ci)
        addGitMetadata(testRunDB, repoName, branchName == "main", branchName, projectName, null, null)
        return testRunDB
    }

    fun addGitMetadata(
        testRunDB: TestRunDB,
        repoName: String,
        isMainBranch: Boolean,
        branchName: String?,
        projectName: String?,
        pullRequestNumber: Int?,
        commitSha: String?
    ): GitMetadataDB {
        val gitMetadata = GitMetadataDB()
        gitMetadata.testRunId = testRunDB.id
        gitMetadata.repoName = repoName
        gitMetadata.orgName = repoName.split("/").first()
        gitMetadata.isMainBranch = isMainBranch
        gitMetadata.branchName = branchName
        gitMetadata.projectName = projectName
        gitMetadata.pullRequestNumber = pullRequestNumber
        gitMetadata.commitSha = commitSha
        gitMetadataDao.insert(gitMetadata)

        return gitMetadata
    }

    fun addResultsMetadata(
        testRunDB: TestRunDB,
        ci: Boolean
    ): ResultsMetadataDB {
        val resultsMetadata = ResultsMetadataDB()
        resultsMetadata.testRunId = testRunDB.id
        resultsMetadata.ci = ci
        resultsMetadataDao.insert(resultsMetadata)

        return resultsMetadata
    }

    fun createSimpleTestRun(publicId: PublicId): TestRunDB =
        createTestRun(
            publicId,
            listOf(
                TestSuiteData(
                    "testSuite1",
                    listOf("testSuite1TestCase1"),
                    listOf(),
                    listOf()
                )
            )
        )

    fun createEmptyTestRun(publicId: PublicId): TestRunDB =
        createTestRun(
            publicId,
            listOf()
        )

    fun createEmptyTestRunInRepo(publicId: PublicId, repoName: String, ci: Boolean, projectName: String?): TestRunDB {
        val testRunDB = createTestRun(
            publicId,
            listOf()
        )
        addResultsMetadata(testRunDB, ci)
        addGitMetadata(testRunDB, repoName, true, "main", projectName, null, null)
        return testRunDB
    }

    fun createSimpleTestRunInRepo(publicId: PublicId, repoName: String, ci: Boolean, projectName: String?): TestRunDB {
        val testRunDB = createSimpleTestRun(publicId)
        addResultsMetadata(testRunDB, ci)
        addGitMetadata(testRunDB, repoName, true, "main", projectName, null, null)
        return testRunDB
    }

    fun createTestRun(publicId: PublicId, createdOn: LocalDate, pinned: Boolean): TestRunDB {
        val testRun = createTestRun(publicId, listOf())
        testRun.createdTimestamp = createdOn.atStartOfDay()
        testRunDao.update(testRun)

        val testRunSystemAttributes = TestRunSystemAttributes(publicId.id, pinned)
        testRunSystemAttributesDao.insert(testRunSystemAttributes)

        return testRun
    }

    fun createTestRunWithCoverageAndGitMetadata(
        publicId: PublicId,
        coverageText: String,
        repoName: String,
        branchName: String = "main",
        projectName: String? = null
    ): TestRunDB = createTestRunWithCoverageAndGitMetadata(
        publicId,
        CoverageFilePayload(coverageText),
        repoName,
        branchName,
        projectName
    )

    fun createTestRunWithCoverageAndGitMetadata(
        publicId: PublicId,
        coverageFilePayload: CoverageFilePayload,
        repoName: String,
        branchName: String = "main",
        projectName: String? = null
    ): TestRunDB {
        val testRunDB = createSimpleTestRun(publicId)
        addGitMetadata(testRunDB, repoName, branchName == "main", branchName, projectName, null, null)
        runBlocking { coverageService.saveReport(coverageFilePayload, publicId) }

        return testRunDB
    }

    fun addTestSuiteGroupToTestRun(testSuiteGroup: TestSuiteGroupDB, testRun: TestRunDB, testSuiteClassNames: List<String>) {
        val testSuiteDBs = testSuiteDao.fetchByTestRunId(testRun.id).filter { it.className in testSuiteClassNames }

        testSuiteDBs.forEach { testSuiteDB ->
            testSuiteDB.testSuiteGroupId = testSuiteGroup.id
            testSuiteDao.update(testSuiteDB)
        }
    }

    fun addTestSuiteGroupToTestRun(groupName: String, testRun: TestRunDB, testSuiteClassNames: List<String>): TestSuiteGroupDB {
        val testSuiteGroup = TestSuiteGroupDB()
        testSuiteGroup.testRunId = testRun.id
        testSuiteGroup.groupName = groupName
        testSuiteGroupDao.insert(testSuiteGroup)

        addTestSuiteGroupToTestRun(testSuiteGroup, testRun, testSuiteClassNames)

        return testSuiteGroup
    }

    fun addAttachment(publicId: PublicId, objectName: String, fileName: String): TestRunAttachment {
        val attachment = TestRunAttachment()
        attachment.testRunPublicId = publicId.id
        attachment.objectName = objectName
        attachment.fileName = fileName
        attachmentDao.insert(attachment)

        return attachment
    }
}

data class TestSuiteData(
    val packageAndClassName: String,
    val passingTestCaseNames: List<String>,
    val failingTestCaseNames: List<String>,
    val skippedTestCaseNames: List<String>,
    val fileName: String? = null
)

fun createTestRun(publicId: PublicId, totalTestCount: Int, cumulativeDuration: BigDecimal = BigDecimal("30.000")): TestRunDB = TestRunDB()
    .setPublicId(publicId.id)
    .setTotalTestCount(totalTestCount)
    .setTotalPassingCount(totalTestCount)
    .setTotalFailureCount(0)
    .setTotalSkippedCount(0)
    .setCumulativeDuration(cumulativeDuration)
    .setAverageDuration(
        if (totalTestCount > 0)
            calculateAverageDuration(cumulativeDuration, totalTestCount)
        else
            cumulativeDuration
    )
    .setSlowestTestCaseDuration(BigDecimal("10.000"))
    .setPassed(true)
    .setCreatedTimestamp(LocalDateTime.now())

fun createTestSuite(testRunId: Long, packageAndClassName: String, idx: Int): TestSuiteDB = TestSuiteDB()
    .setTestRunId(testRunId)
    .setPackageName(parsePackageAndClassName(packageAndClassName).first)
    .setClassName(parsePackageAndClassName(packageAndClassName).second)
    .setIdx(idx)
    .setTestCount(6)
    .setPassingCount(3)
    .setFailureCount(2)
    .setSkippedCount(1)
    .setDuration(BigDecimal.TEN)
    .setStartTs(LocalDateTime.now())
    .setHostname("hostname")

fun createTestCase(testSuiteId: Long, name: String, idx: Int, passed: Boolean): TestCaseDB = TestCaseDB()
    .setTestSuiteId(testSuiteId)
    .setName(name)
    .setIdx(idx)
    .setClassName("${name}ClassName")
    .setDuration(BigDecimal("2.5"))
    .setPassed(passed)
    .setSkipped(false)

fun createTestFailure(testCaseId: Long, testCaseName: String): TestFailureDB = TestFailureDB()
    .setTestCaseId(testCaseId)
    .setFailureMessage("$testCaseName failure message")
    .setFailureText("$testCaseName failure text")
    .setFailureType("$testCaseName failure type")
