package projektor.metrics

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.verification.LoggedRequest

class MetricsStubber {
    private val wireMockServer = WireMockServer(wireMockConfig().dynamicPort())

    fun port() = wireMockServer.port()

    fun start() {
        wireMockServer.start()
    }

    fun reset() {
        wireMockServer.resetAll()

        wireMockServer.stubFor(WireMock.post(WireMock.urlMatching("/query.*")).willReturn(WireMock.aResponse().withStatus(200)))
        wireMockServer.stubFor(WireMock.post(WireMock.urlMatching("/write.*")).willReturn(WireMock.aResponse().withStatus(200)))
    }

    fun stop() {
        wireMockServer.stop()
    }

    fun findCreateMetricsDatabaseRequests(): List<LoggedRequest> =
        wireMockServer.findAll(WireMock.postRequestedFor(WireMock.urlMatching("/query.*")))

    fun findWriteMetricsRequests(): List<LoggedRequest> =
        wireMockServer.findAll(WireMock.postRequestedFor(WireMock.urlMatching("/write.*")))

    fun findWriteMetricsRequestsForMetric(metricContents: String) =
        findWriteMetricsRequests().filter { it.bodyAsString.contains(metricContents) }

    fun findWriteMetricsRequestForCounterMetric(metricName: String) =
        findWriteMetricsRequestsForMetric("$metricName,metric_type=counter")

    fun verifyWriteMetricsRequestForCounterMetric(metricName: String, expectedCount: Int): Boolean {
        val metricWriteRequests = findWriteMetricsRequestsForMetric(metricName)

        val metricCounts = metricWriteRequests.map { it.bodyAsString.substringAfter("$metricName,metric_type=counter value=").substring(0, 1).toInt() }

        return metricCounts.any { it >= expectedCount }
    }

    fun findWriteMetricsRequestForCounterMetric(metricName: String, expectedCount: Int) =
        findWriteMetricsRequestsForMetric("$metricName,metric_type=counter value=$expectedCount")
}
