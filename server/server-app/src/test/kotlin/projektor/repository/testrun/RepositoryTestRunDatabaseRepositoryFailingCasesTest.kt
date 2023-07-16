package projektor.repository.testrun

import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.server.api.TestCase
import projektor.server.api.repository.BranchType
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

class RepositoryTestRunDatabaseRepositoryFailingCasesTest : DatabaseRepositoryTestCase() {

    @Test
    fun `should find failing test cases in repo and CI without project name`() {
        val repositoryTestRunDatabaseRepository = RepositoryTestRunDatabaseRepository(dslContext)

        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = null

        val firstRunCITruePublicId = randomPublicId()
        val secondRunCITruePublicId = randomPublicId()

        val nonCIPublicId = randomPublicId()

        val ciInOtherRepoPublicId = randomPublicId()

        val testSuiteDataList = listOf(
            TestSuiteData(
                "projektor.failingTestSuite1",
                listOf("passing1"),
                listOf("failing1", "failing2"),
                listOf()
            ),
            TestSuiteData(
                "projektor.passingTestSuite",
                listOf("passing2"),
                listOf(),
                listOf()
            ),
            TestSuiteData(
                "projektor.failingTestSuite2",
                listOf("passing3"),
                listOf("failing3", "failing4"),
                listOf()
            )
        )

        val maxRuns = 4

        testRunDBGenerator.createTestRunInRepo(firstRunCITruePublicId, testSuiteDataList, repoName, true, projectName)
        testRunDBGenerator.createTestRunInRepo(secondRunCITruePublicId, testSuiteDataList, repoName, true, projectName)
        testRunDBGenerator.createTestRunInRepo(nonCIPublicId, testSuiteDataList, repoName, false, projectName)
        testRunDBGenerator.createTestRunInRepo(ciInOtherRepoPublicId, testSuiteDataList, "$orgName/another-project", true, projectName)

        val failingTestCases = runBlocking {
            repositoryTestRunDatabaseRepository.fetchRepositoryFailingTestCases(
                repoName,
                projectName,
                maxRuns,
                BranchType.ALL
            )
        }

        val failingTestCaseNames = failingTestCases.map(TestCase::fullName)

        expectThat(failingTestCaseNames)
            .contains(
                "projektor.failing1ClassName.failing1",
                "projektor.failing2ClassName.failing2",
                "projektor.failing3ClassName.failing3",
                "projektor.failing4ClassName.failing4",
            )

        expectThat(failingTestCaseNames).hasSize(8)

        val failing1Cases = failingTestCases.filter { it.name == "failing1" }
        expectThat(failing1Cases)
            .hasSize(2)
            .and {
                any { get { publicId }.isEqualTo(firstRunCITruePublicId.id) }
                any { get { publicId }.isEqualTo(secondRunCITruePublicId.id) }
            }

        val failing2Cases = failingTestCases.filter { it.name == "failing2" }
        expectThat(failing2Cases)
            .hasSize(2)
            .and {
                any { get { publicId }.isEqualTo(firstRunCITruePublicId.id) }
                any { get { publicId }.isEqualTo(secondRunCITruePublicId.id) }
            }

        val failing3Cases = failingTestCases.filter { it.name == "failing3" }
        expectThat(failing3Cases)
            .hasSize(2)
            .and {
                any { get { publicId }.isEqualTo(firstRunCITruePublicId.id) }
                any { get { publicId }.isEqualTo(secondRunCITruePublicId.id) }
            }

        val failing4Cases = failingTestCases.filter { it.name == "failing4" }
        expectThat(failing4Cases)
            .hasSize(2)
            .and {
                any { get { publicId }.isEqualTo(firstRunCITruePublicId.id) }
                any { get { publicId }.isEqualTo(secondRunCITruePublicId.id) }
            }
    }

    @Test
    fun `should filter out older test runs`() {
        val repositoryTestRunDatabaseRepository = RepositoryTestRunDatabaseRepository(dslContext)

        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = null

        val firstRunCITruePublicId = randomPublicId()
        val secondRunCITruePublicId = randomPublicId()
        val thirdRunCITruePublicId = randomPublicId()

        val testSuiteDataList = listOf(
            TestSuiteData(
                "projektor.failingTestSuite1",
                listOf("passing1"),
                listOf("failing1"),
                listOf()
            ),
            TestSuiteData(
                "projektor.passingTestSuite",
                listOf("passing2"),
                listOf(),
                listOf()
            )
        )

        listOf(firstRunCITruePublicId, secondRunCITruePublicId, thirdRunCITruePublicId).forEach { publicId ->
            testRunDBGenerator.createTestRunInRepo(publicId, testSuiteDataList, repoName, true, projectName)
        }

        val failingTestCasesMax1 = runBlocking {
            repositoryTestRunDatabaseRepository.fetchRepositoryFailingTestCases(
                repoName,
                projectName,
                1,
                BranchType.ALL
            )
        }

        expectThat(failingTestCasesMax1)
            .hasSize(1)

        expectThat(failingTestCasesMax1[0].publicId).isEqualTo(thirdRunCITruePublicId.id)

        val failingTestCasesMax2 = runBlocking {
            repositoryTestRunDatabaseRepository.fetchRepositoryFailingTestCases(
                repoName,
                projectName,
                2,
                BranchType.ALL
            )
        }

        expectThat(failingTestCasesMax2)
            .hasSize(2)

        expectThat(failingTestCasesMax2[0].publicId).isEqualTo(thirdRunCITruePublicId.id)
        expectThat(failingTestCasesMax2[1].publicId).isEqualTo(secondRunCITruePublicId.id)
    }

    @Test
    fun `should find failing tests in mainline only`() {
        val repositoryTestRunDatabaseRepository = RepositoryTestRunDatabaseRepository(dslContext)

        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = null

        val failingMainlinePublicIds = (1..3).map { randomPublicId() }
        val failingMainlineTestSuiteDataList = listOf(
            TestSuiteData(
                "projektor.failingMainlineTestSuite1",
                listOf("passing1"),
                listOf("failingMainline1"),
                listOf()
            ),
            TestSuiteData(
                "projektor.failingMainlineTestSuite2",
                listOf("passing2"),
                listOf("failingMainline2"),
                listOf()
            )
        )

        val failingBranchPublicIds = (1..3).map { randomPublicId() }
        val failingBranchTestSuiteDataList = listOf(
            TestSuiteData(
                "projektor.failingBranchTestSuite1",
                listOf("passing1"),
                listOf("failingBranch1"),
                listOf()
            ),
            TestSuiteData(
                "projektor.failingBranchTestSuite2",
                listOf("passing2"),
                listOf("failingBranch2"),
                listOf()
            )
        )

        val passingPublicIds = (1..7).map { randomPublicId() }
        val passingTestSuiteDataList = listOf(
            TestSuiteData(
                "projektor.passingTestSuite",
                listOf("passing1", "passing2"),
                listOf(),
                listOf()
            )
        )

        failingMainlinePublicIds.forEach { publicId ->
            testRunDBGenerator.createTestRunInRepo(publicId, failingMainlineTestSuiteDataList, repoName, true, projectName, "main")
        }
        failingBranchPublicIds.forEach { publicId ->
            testRunDBGenerator.createTestRunInRepo(publicId, failingBranchTestSuiteDataList, repoName, true, projectName, "my-branch")
        }
        passingPublicIds.forEach { publicId ->
            testRunDBGenerator.createTestRunInRepo(publicId, passingTestSuiteDataList, repoName, true, projectName)
        }

        val failingTestCases = runBlocking {
            repositoryTestRunDatabaseRepository.fetchRepositoryFailingTestCases(
                repoName,
                projectName,
                100,
                BranchType.MAINLINE
            )
        }

        val failingTestCaseNames = failingTestCases.map(TestCase::fullName)

        expectThat(failingTestCaseNames)
            .contains(
                "projektor.failingMainline1ClassName.failingMainline1",
                "projektor.failingMainline2ClassName.failingMainline2"
            )
            .not().contains(
                "projektor.failingBranch1ClassName.failingBranch1",
                "projektor.failingBranch2ClassName.failingBranch2"
            )
    }

    @Test
    fun `should find failing tests in all branches`() {
        val repositoryTestRunDatabaseRepository = RepositoryTestRunDatabaseRepository(dslContext)

        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = null

        val failingMainlinePublicIds = (1..3).map { randomPublicId() }
        val failingMainlineTestSuiteDataList = listOf(
            TestSuiteData(
                "projektor.failingMainlineTestSuite1",
                listOf("passing1"),
                listOf("failingMainline1"),
                listOf()
            ),
            TestSuiteData(
                "projektor.failingMainlineTestSuite2",
                listOf("passing2"),
                listOf("failingMainline2"),
                listOf()
            )
        )

        val failingBranchPublicIds = (1..3).map { randomPublicId() }
        val failingBranchTestSuiteDataList = listOf(
            TestSuiteData(
                "projektor.failingBranchTestSuite1",
                listOf("passing1"),
                listOf("failingBranch1"),
                listOf()
            ),
            TestSuiteData(
                "projektor.failingBranchTestSuite2",
                listOf("passing2"),
                listOf("failingBranch2"),
                listOf()
            )
        )

        val passingPublicIds = (1..7).map { randomPublicId() }
        val passingTestSuiteDataList = listOf(
            TestSuiteData(
                "projektor.passingTestSuite",
                listOf("passing1", "passing2"),
                listOf(),
                listOf()
            )
        )

        failingMainlinePublicIds.forEach { publicId ->
            testRunDBGenerator.createTestRunInRepo(publicId, failingMainlineTestSuiteDataList, repoName, true, projectName, "main")
        }
        failingBranchPublicIds.forEach { publicId ->
            testRunDBGenerator.createTestRunInRepo(publicId, failingBranchTestSuiteDataList, repoName, true, projectName, "my-branch")
        }
        passingPublicIds.forEach { publicId ->
            testRunDBGenerator.createTestRunInRepo(publicId, passingTestSuiteDataList, repoName, true, projectName)
        }

        val failingTestCases = runBlocking {
            repositoryTestRunDatabaseRepository.fetchRepositoryFailingTestCases(
                repoName,
                projectName,
                100,
                BranchType.ALL
            )
        }

        val failingTestCaseNames = failingTestCases.map(TestCase::fullName)

        expectThat(failingTestCaseNames)
            .contains(
                "projektor.failingMainline1ClassName.failingMainline1",
                "projektor.failingMainline2ClassName.failingMainline2"
            )
            .contains(
                "projektor.failingBranch1ClassName.failingBranch1",
                "projektor.failingBranch2ClassName.failingBranch2"
            )
    }
}
