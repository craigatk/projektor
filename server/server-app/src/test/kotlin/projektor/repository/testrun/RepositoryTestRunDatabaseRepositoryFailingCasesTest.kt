package projektor.repository.testrun

import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.server.api.TestCase
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
                TestSuiteData("projektor.failingTestSuite1",
                        listOf("passing1"),
                        listOf("failing1", "failing2"),
                        listOf()
                ),
                TestSuiteData("projektor.passingTestSuite",
                        listOf("passing2"),
                        listOf(),
                        listOf()
                ),
                TestSuiteData("projektor.failingTestSuite2",
                        listOf("passing3"),
                        listOf("failing3", "failing4"),
                        listOf()
                )
        )

        val firstTestRun = testRunDBGenerator.createTestRun(firstRunCITruePublicId, testSuiteDataList)
        testRunDBGenerator.addResultsMetadata(firstTestRun, true)
        testRunDBGenerator.addGitMetadata(firstTestRun, repoName, true, "main", projectName)

        val secondTestRun = testRunDBGenerator.createTestRun(secondRunCITruePublicId, testSuiteDataList)
        testRunDBGenerator.addResultsMetadata(secondTestRun, true)
        testRunDBGenerator.addGitMetadata(secondTestRun, repoName, true, "main", projectName)

        val nonCITestRun = testRunDBGenerator.createTestRun(nonCIPublicId, testSuiteDataList)
        testRunDBGenerator.addResultsMetadata(nonCITestRun, false)
        testRunDBGenerator.addGitMetadata(nonCITestRun, repoName, true, "main", projectName)

        val ciInAnotherRepoTestRun = testRunDBGenerator.createTestRun(ciInOtherRepoPublicId, testSuiteDataList)
        testRunDBGenerator.addResultsMetadata(ciInAnotherRepoTestRun, true)
        testRunDBGenerator.addGitMetadata(ciInAnotherRepoTestRun, "$orgName/another-repo", true, "main", projectName)

        val failingTestCases = runBlocking { repositoryTestRunDatabaseRepository.fetchRepositoryFailingTestCases(repoName, projectName) }

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
}