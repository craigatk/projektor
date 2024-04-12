package projektor.notification.github

import com.github.tomakehurst.wiremock.WireMockServer
import io.kotest.core.listeners.TestListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult

class WireMockTestListener(private val wireMockServer: WireMockServer) : TestListener {
    override suspend fun beforeTest(testCase: TestCase) {
        wireMockServer.start()
    }

    override suspend fun beforeEach(testCase: TestCase) {
        wireMockServer.resetAll()
    }

    override suspend fun afterTest(
        testCase: TestCase,
        result: TestResult,
    ) {
        wireMockServer.stop()
    }
}
