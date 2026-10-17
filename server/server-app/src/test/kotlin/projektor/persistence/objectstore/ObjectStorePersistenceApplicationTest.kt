package projektor.persistence.objectstore

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import org.apache.commons.lang3.RandomStringUtils
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.Test
import org.koin.ktor.ext.get
import projektor.ApplicationTestCase
import projektor.ApplicationTestCaseConfig
import projektor.cleanup.CleanupScheduledJob
import projektor.mcp.callMcpTool
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.grouped.model.GitMetadata
import projektor.parser.grouped.model.ResultsMetadata
import projektor.schedule.Scheduler
import projektor.server.api.TestCase
import projektor.server.api.TestRun
import projektor.server.api.TestRunSummary
import projektor.server.api.TestSuite
import projektor.server.api.attachments.Attachments
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
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isGreaterThan
import strikt.assertions.isNotEmpty
import strikt.assertions.isTrue
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit
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

    @Test
    fun `should save and fetch attachments with object-store persistence`() {
        val repoName = "org-${RandomStringUtils.randomAlphabetic(10).lowercase()}/repo"

        projektorTestApplication(ApplicationTestCaseConfig(objectStorePersistence = true, attachmentsEnabled = true)) {
            val publicId = publishResults(client, repoName)
            val attachmentBytes = File("src/test/resources/test-attachment.txt").readBytes()

            val postResponse =
                client.post("/run/$publicId/attachments/test-attachment.txt") {
                    headers { append("content-length", attachmentBytes.size.toString()) }
                    setBody(attachmentBytes)
                }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.OK)

            val attachments = client.fetch<Attachments>("/run/$publicId/attachments")
            expectThat(attachments.attachments.map { it.fileName }).isEqualTo(listOf("test-attachment.txt"))

            expectThat(
                client.get("/run/$publicId/attachments/test-attachment.txt").bodyAsText(),
            ).isEqualTo("Here is a test attachment file")
        }
    }

    @Test
    fun `should return pull request failing test context over MCP with object-store persistence`() {
        val orgName = "org-${RandomStringUtils.randomAlphabetic(10).lowercase()}"
        val repoPart = "repo"

        projektorTestApplication(ApplicationTestCaseConfig(objectStorePersistence = true)) {
            val publicId = publishResults(client, "$orgName/$repoPart", branchName = "feature", pullRequestNumber = 7)

            val response =
                client.callMcpTool(
                    "get_pull_request_failing_test_context",
                    mapOf("orgName" to orgName, "repoName" to repoPart, "pullRequestNumber" to 7),
                )

            expectThat(response.isError).isFalse()
            expectThat(response.text).contains(publicId)
            expectThat(response.text).contains("projektor.example.spock.FailingSpec.should fail")
        }
    }

    @Test
    fun `cleanup job should delete old test runs and attachments with object-store persistence`() {
        val repoName = "org-${RandomStringUtils.randomAlphabetic(10).lowercase()}/repo"

        projektorTestApplication(
            ApplicationTestCaseConfig(objectStorePersistence = true, attachmentsEnabled = true, reportCleanupMaxAgeDays = 30),
        ) {
            val oldPublicId = publishResults(client, repoName, createdTimestamp = Instant.now().minus(40, ChronoUnit.DAYS))
            val newPublicId = publishResults(client, repoName)

            val attachmentBytes = File("src/test/resources/test-attachment.txt").readBytes()
            listOf(oldPublicId, newPublicId).forEach { publicId ->
                client.post("/run/$publicId/attachments/test-attachment.txt") {
                    headers { append("content-length", attachmentBytes.size.toString()) }
                    setBody(attachmentBytes)
                }
            }

            val scheduler: Scheduler = getApplication().get()
            scheduler.findScheduledJob(CleanupScheduledJob.CLEANUP_JOB_NAME)!!.runnable.run()

            expectThat(client.get("/run/$oldPublicId").status).isEqualTo(HttpStatusCode.NotFound)
            expectThat(client.fetch<Attachments>("/run/$oldPublicId/attachments").attachments).isEmpty()
            expectThat(client.fetch<ResultsProcessing>("/results/$oldPublicId/status").status).isEqualTo(ResultsProcessingStatus.DELETED)

            expectThat(client.get("/run/$newPublicId").status).isEqualTo(HttpStatusCode.OK)
            expectThat(client.fetch<Attachments>("/run/$newPublicId/attachments").attachments).hasSize(1)

            val timeline = client.fetch<RepositoryTestRunTimeline>("/repo/$repoName/timeline")
            expectThat(timeline.timelineEntries.map { it.publicId }).isEqualTo(listOf(newPublicId))
        }
    }

    private suspend fun publishResults(
        client: HttpClient,
        repoName: String,
        branchName: String = "main",
        pullRequestNumber: Int? = null,
        createdTimestamp: Instant? = null,
    ): String {
        val gitMetadata = GitMetadata()
        gitMetadata.repoName = repoName
        gitMetadata.branchName = branchName
        gitMetadata.isMainBranch = branchName == "main"
        gitMetadata.pullRequestNumber = pullRequestNumber
        val metadata = ResultsMetadata()
        metadata.git = gitMetadata
        metadata.ci = true
        metadata.createdTimestamp = createdTimestamp

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

        // The pull request number is saved after the processing status is set to success
        if (pullRequestNumber != null) {
            await.atMost(30, TimeUnit.SECONDS) until {
                kotlinx.coroutines.runBlocking {
                    client.fetch<TestRunGitMetadata>("/run/$publicId/metadata/git").pullRequestNumber
                } == pullRequestNumber
            }
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
