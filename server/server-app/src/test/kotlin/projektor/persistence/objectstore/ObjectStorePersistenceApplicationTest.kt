package projektor.persistence.objectstore

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import org.apache.commons.lang3.RandomStringUtils
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.ApplicationTestCaseConfig
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.grouped.model.GitMetadata
import projektor.parser.grouped.model.ResultsMetadata
import projektor.server.api.TestCase
import projektor.server.api.TestRun
import projektor.server.api.TestRunSummary
import projektor.server.api.TestSuite
import projektor.server.api.attributes.TestRunSystemAttributes
import projektor.server.api.coverage.Coverage
import projektor.server.api.history.TestCaseHistory
import projektor.server.api.metadata.TestRunGitMetadata
import projektor.server.api.repository.RepositoryTestRunTimeline
import projektor.server.api.repository.coverage.RepositoryCoverageTimeline
import projektor.server.api.results.ResultsProcessing
import projektor.server.api.results.ResultsProcessingStatus
import projektor.server.api.results.SaveResultsResponse
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan
import strikt.assertions.isNotEmpty
import strikt.assertions.isTrue
import java.util.concurrent.TimeUnit

/**
 * Runs the full application on the object-store persistence backend, publishing results over HTTP and
 * fetching them back through the same endpoints the UI uses.
 */
class ObjectStorePersistenceApplicationTest : ApplicationTestCase() {
    @Test
    fun `should save and fetch test results with object-store persistence`() {
        val orgName = "org-${RandomStringUtils.randomAlphabetic(10).lowercase()}"
        val repoPart = "repo"
        val repoName = "$orgName/$repoPart"

        projektorTestApplication(ApplicationTestCaseConfig(objectStorePersistence = true)) {
            val firstPublicId = publishResults(client, repoName)
            val secondPublicId = publishResults(client, repoName)

            val testRun = client.fetch<TestRun>("/run/$secondPublicId")
            expectThat(testRun.testSuites.orEmpty()).hasSize(3)

            val summary = client.fetch<TestRunSummary>("/run/$secondPublicId/summary")
            expectThat(summary.totalFailureCount).isGreaterThan(0)

            val failedTestCases = client.fetch<List<TestCase>>("/run/$secondPublicId/cases/failed")
            expectThat(failedTestCases).isNotEmpty()

            val failedTestCase = failedTestCases.first()
            val testSuite = client.fetch<TestSuite>("/run/$secondPublicId/suite/${failedTestCase.testSuiteIdx}")
            expectThat(testSuite.testCases.orEmpty()).isNotEmpty()

            val history =
                client.fetch<TestCaseHistory>(
                    "/run/$secondPublicId/suite/${failedTestCase.testSuiteIdx}/case/${failedTestCase.idx}/history",
                )
            expectThat(history.entries.map { it.publicId }).isEqualTo(listOf(secondPublicId, firstPublicId))

            val gitMetadata = client.fetch<TestRunGitMetadata>("/run/$secondPublicId/metadata/git")
            expectThat(gitMetadata.repoName).isEqualTo(repoName)

            val coverage = client.fetch<Coverage>("/run/$secondPublicId/coverage")
            expectThat(coverage.groups).isNotEmpty()
            expectThat(coverage.previousTestRunId).isEqualTo(firstPublicId)

            expectThat(client.get("/run/$secondPublicId/previous").bodyAsText()).contains(firstPublicId)

            val timeline = client.fetch<RepositoryTestRunTimeline>("/repo/$orgName/$repoPart/timeline")
            expectThat(timeline.timelineEntries.map { it.publicId }).isEqualTo(listOf(firstPublicId, secondPublicId))

            val coverageTimeline = client.fetch<RepositoryCoverageTimeline>("/repo/$orgName/$repoPart/coverage/timeline")
            expectThat(coverageTimeline.timelineEntries).hasSize(2)

            expectThat(client.get("/org/$orgName/coverage").bodyAsText()).contains(repoName)

            expectThat(client.post("/run/$firstPublicId/attributes/pin").status).isEqualTo(HttpStatusCode.OK)
            expectThat(client.fetch<TestRunSystemAttributes>("/run/$firstPublicId/attributes").pinned).isTrue()

            expectThat(client.get("/failures/recent").status).isEqualTo(HttpStatusCode.OK)
        }
    }

    private suspend fun publishResults(
        client: HttpClient,
        repoName: String,
    ): String {
        val gitMetadata = GitMetadata()
        gitMetadata.repoName = repoName
        gitMetadata.branchName = "main"
        gitMetadata.isMainBranch = true
        val metadata = ResultsMetadata()
        metadata.git = gitMetadata
        metadata.ci = true

        val requestBody =
            GroupedResultsXmlLoader().resultsWithCoverage(
                listOf(resultsXmlLoader.passing(), resultsXmlLoader.failing(), resultsXmlLoader.output()),
                listOf(cloverXmlLoader.coverageFilesTable()),
                metadata,
            )

        val response = client.postGroupedResultsJSON(requestBody)
        val publicId = objectMapper.readValue(response.bodyAsText(), SaveResultsResponse::class.java).id

        await.atMost(30, TimeUnit.SECONDS) until {
            kotlinx.coroutines.runBlocking {
                client.fetch<ResultsProcessing>(
                    "/results/$publicId/status",
                ).status
            } == ResultsProcessingStatus.SUCCESS
        }

        return publicId
    }

    private suspend inline fun <reified T> HttpClient.fetch(path: String): T {
        val response = get(path)
        expectThat(response.status).describedAs("GET $path").isEqualTo(HttpStatusCode.OK)
        return objectMapper.readValue(
            response.bodyAsText(),
            objectMapper.typeFactory.constructType(object : com.fasterxml.jackson.core.type.TypeReference<T>() {}),
        )
    }
}
