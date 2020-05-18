package projektor.incomingresults

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import java.time.Duration
import kotlin.test.assertNotNull
import org.awaitility.Awaitility.waitAtMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.awaitility.kotlin.untilNotNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.metrics.MetricsStubber
import projektor.parser.GroupedResultsXmlLoader
import projektor.server.api.results.ResultsProcessingStatus
import projektor.server.api.results.SaveResultsResponse
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

@KtorExperimentalAPI
class SaveGroupedResultsApplicationTest : ApplicationTestCase() {
    private val metricsStubber = MetricsStubber()

    @BeforeEach
    fun start() {
        metricsStubber.start()
    }

    @AfterEach
    fun stop() {
        metricsStubber.stop()
    }

    @Test
    fun `should save grouped test results`() {
        val requestBody = GroupedResultsXmlLoader().passingGroupedResults()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(requestBody)
            }.apply {
                val resultsResponse = objectMapper.readValue(response.content, SaveResultsResponse::class.java)

                val publicId = resultsResponse.id
                assertNotNull(publicId)
                expectThat(resultsResponse.uri).isEqualTo("/tests/$publicId")

                val testRun = await untilNotNull { testRunDao.fetchOneByPublicId(publicId) }
                assertNotNull(testRun)

                val testSuites = testSuiteDao.fetchByTestRunId(testRun.id)
                expectThat(testSuites).hasSize(3)

                expectThat(testSuites.find { it.idx == 1 }).isNotNull()
                expectThat(testSuites.find { it.idx == 2 }).isNotNull()
                expectThat(testSuites.find { it.idx == 3 }).isNotNull()

                val testSuiteGroups = testSuiteGroupDao.fetchByTestRunId(testRun.id)
                expectThat(testSuiteGroups)
                        .hasSize(2)

                val testSuiteGroup1 = testSuiteGroups.find { it.groupName == "Group1" }
                assertNotNull(testSuiteGroup1)
                expectThat(testSuiteDao.fetchByTestSuiteGroupId(testSuiteGroup1.id)).hasSize(2)

                val testSuiteGroup2 = testSuiteGroups.find { it.groupName == "Group2" }
                assertNotNull(testSuiteGroup2)
                expectThat(testSuiteDao.fetchByTestSuiteGroupId(testSuiteGroup2.id)).hasSize(1)

                await until { resultsProcessingDao.fetchOneByPublicId(publicId).status == ResultsProcessingStatus.SUCCESS.name }
            }
        }
    }

    @Test
    fun `should record metrics when saving grouped test results`() {
        metricsEnabled = true
        metricsPort = metricsStubber.port()

        val requestBody = GroupedResultsXmlLoader().passingGroupedResults()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(requestBody)
            }.apply {
                val resultsResponse = objectMapper.readValue(response.content, SaveResultsResponse::class.java)

                val publicId = resultsResponse.id
                assertNotNull(publicId)

                await untilNotNull { testRunDao.fetchOneByPublicId(publicId) }

                await until { resultsProcessingDao.fetchOneByPublicId(publicId).status == ResultsProcessingStatus.SUCCESS.name }

                waitAtMost(Duration.ofSeconds(30)) until { metricsStubber.findWriteMetricsRequestForCounterMetric("grouped_results_process_success", 1).isNotEmpty() }
                await until { metricsStubber.findWriteMetricsRequestForCounterMetric("results_process_success", 1).isNotEmpty() }

                await until { metricsStubber.findWriteMetricsRequestForCounterMetric("grouped_results_process_failure", 0).isNotEmpty() }
                await until { metricsStubber.findWriteMetricsRequestForCounterMetric("results_process_failure", 0).isNotEmpty() }
            }
        }
    }
}
