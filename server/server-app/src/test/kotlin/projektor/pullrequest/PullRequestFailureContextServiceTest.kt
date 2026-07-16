package projektor.pullrequest

import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import org.koin.test.inject
import projektor.DatabaseRepositoryTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class PullRequestFailureContextServiceTest : DatabaseRepositoryTestCase() {
    @Test
    fun `should build failure context for most recent test run in pull request`() {
        val pullRequestFailureContextService: PullRequestFailureContextService by inject()

        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "repo"
        val pullRequestNumber = 7

        val publicId = randomPublicId()

        val testRunDB =
            testRunDBGenerator.createTestRun(
                publicId,
                listOf(
                    TestSuiteData(
                        "testSuite1",
                        listOf(),
                        listOf("failingTestSuite1TestCase1"),
                        listOf(),
                    ),
                ),
            )
        testRunDBGenerator.addResultsMetadata(testRunDB, true)
        testRunDBGenerator.addGitMetadata(
            testRunDB,
            "$orgName/$repoName",
            false,
            "feature",
            null,
            pullRequestNumber,
            null,
        )

        val failureContext =
            runBlocking { pullRequestFailureContextService.fetchFailureContext(orgName, repoName, pullRequestNumber) }

        expectThat(failureContext!!.testRunPublicId).isEqualTo(publicId.id)
        expectThat(failureContext.failingTestCases).hasSize(1)
        expectThat(failureContext.failingTestCases[0].debugContextMarkdown).contains("failingTestSuite1TestCase1 failure message")
    }

    @Test
    fun `should return null when no test run found for pull request`() {
        val pullRequestFailureContextService: PullRequestFailureContextService by inject()

        val orgName = RandomStringUtils.randomAlphabetic(12)

        val failureContext =
            runBlocking { pullRequestFailureContextService.fetchFailureContext(orgName, "repo", 123) }

        expectThat(failureContext).isNull()
    }
}
