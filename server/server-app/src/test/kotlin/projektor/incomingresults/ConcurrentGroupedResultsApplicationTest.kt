package projektor.incomingresults

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.server.testing.ApplicationTestBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.apache.commons.lang3.RandomStringUtils
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.ApplicationTestCaseConfig
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.grouped.model.GitMetadata
import projektor.parser.grouped.model.ResultsMetadata
import projektor.results.processor.TestResultsProcessor
import projektor.server.api.TestRun
import projektor.server.api.results.SaveResultsResponse
import strikt.api.expectThat
import strikt.assertions.hasSize
import java.util.concurrent.TimeUnit

/**
 * Parallel CI jobs publishing results with the same group should end up in one test run,
 * even when their first uploads arrive at the same time.
 */
class ConcurrentGroupedResultsApplicationTest : ApplicationTestCase() {
    // Test cases in GroupedResultsXmlLoader.passingGroupedResults()
    private val testCountPerUpload =
        TestResultsProcessor()
            .parseResultsBlob(resultsXmlLoader.passing() + resultsXmlLoader.longOutput() + resultsXmlLoader.output())
            .sumOf { it.tests }

    @Test
    fun `concurrent first uploads with the same group should share one test run with database persistence`() =
        projektorTestApplication { expectConcurrentUploadsShareTestRun() }

    @Test
    fun `concurrent first uploads with the same group should share one test run with object-store persistence`() =
        projektorTestApplication(ApplicationTestCaseConfig(objectStorePersistence = true)) { expectConcurrentUploadsShareTestRun() }

    private suspend fun ApplicationTestBuilder.expectConcurrentUploadsShareTestRun() {
        val repoName = "org-${RandomStringUtils.randomAlphabetic(10).lowercase()}/repo"
        val uploadCount = 4

        repeat(ATTEMPTS) { attempt ->
            val group = "B$attempt"

            val requestBodies =
                (1..uploadCount).map {
                    val gitMetadata = GitMetadata()
                    gitMetadata.repoName = repoName
                    gitMetadata.branchName = "main"
                    gitMetadata.isMainBranch = true
                    val metadata = ResultsMetadata()
                    metadata.git = gitMetadata
                    metadata.ci = true
                    metadata.group = group
                    GroupedResultsXmlLoader().passingGroupedResults(metadata)
                }

            val publicIds =
                coroutineScope {
                    requestBodies.map { requestBody ->
                        async(Dispatchers.IO) {
                            val response = client.postGroupedResultsJSON(requestBody)
                            objectMapper.readValue(response.bodyAsText(), SaveResultsResponse::class.java).id
                        }
                    }.awaitAll()
                }

            expectThat(publicIds.distinct()).describedAs("public IDs for group $group").hasSize(1)

            // Every upload's test suites end up in the shared test run
            val publicId = publicIds.first()
            await.atMost(30, TimeUnit.SECONDS) until {
                kotlinx.coroutines.runBlocking {
                    val response = client.get("/run/$publicId")
                    response.status.value == 200 &&
                        objectMapper.readValue(
                            response.bodyAsText(),
                            TestRun::class.java,
                        ).summary.totalTestCount == uploadCount * testCountPerUpload
                }
            }
        }
    }

    companion object {
        private const val ATTEMPTS = 3
    }
}
