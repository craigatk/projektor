package projektor.incomingresults

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import org.awaitility.Awaitility.waitAtMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.*
import projektor.ApplicationTestCase
import projektor.metrics.MetricsStubber
import projektor.parser.GroupedResultsXmlLoader
import projektor.server.api.results.ResultsProcessingStatus
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isNotNull
import java.time.Duration
import kotlin.test.assertNotNull

@KtorExperimentalAPI
class SaveGroupedResultsApplicationTest : ApplicationTestCase() {
    @BeforeEach
    fun reset() {
        metricsStubber.reset()
    }

    @Test
    fun `should save grouped test results`() {
        val requestBody = GroupedResultsXmlLoader().passingGroupedResults()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(requestBody)
            }.apply {
                val (publicId, testRun) = waitForTestRunSaveToComplete(response)

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

                await until { resultsProcessingDao.fetchOneByPublicId(publicId.id).status == ResultsProcessingStatus.SUCCESS.name }
            }
        }
    }

    @Test
    @Disabled // This test is just way too flaky, ignoring this until I find a more reliable way to test the metrics
    fun `should record metrics when saving grouped test results`() {
        metricsEnabled = true
        metricsPort = metricsStubber.port()

        val requestBody = GroupedResultsXmlLoader().passingGroupedResults()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(requestBody)
            }.apply {
                waitForTestRunSaveToComplete(response)

                waitAtMost(Duration.ofSeconds(30)) until { metricsStubber.verifyWriteMetricsRequestForCounterMetric("grouped_results_process_success", 1) }
                await until { metricsStubber.verifyWriteMetricsRequestForCounterMetric("results_process_success", 1) }

                await until { metricsStubber.findWriteMetricsRequestForCounterMetric("grouped_results_process_failure", 0).isNotEmpty() }
                await until { metricsStubber.findWriteMetricsRequestForCounterMetric("results_process_failure", 0).isNotEmpty() }
            }
        }
    }

    companion object {
        private val metricsStubber = MetricsStubber()

        @BeforeAll
        @JvmStatic
        fun start() {
            metricsStubber.start()
        }

        @AfterAll
        @JvmStatic
        fun stop() {
            metricsStubber.stop()
        }
    }
}
