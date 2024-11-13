package projektor.testrun

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.TestRunSummary
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.assertNotNull
import projektor.database.generated.tables.pojos.TestRun as TestRunDB

class GetTestRunSummaryApplicationTest : ApplicationTestCase() {
    override fun autoStartServer(): Boolean = true

    @Test
    fun shouldFetchTestRunSummaryFromDatabase() =
        testSuspend {
            val publicId = randomPublicId()

            val testRun =
                TestRunDB()
                    .setPublicId(publicId.id)
                    .setTotalTestCount(6)
                    .setTotalPassingCount(4)
                    .setTotalFailureCount(2)
                    .setTotalSkippedCount(1)
                    .setCumulativeDuration(BigDecimal("30.000"))
                    .setAverageDuration(BigDecimal("5.000"))
                    .setSlowestTestCaseDuration(BigDecimal("10.000"))
                    .setPassed(false)
                    .setCreatedTimestamp(LocalDateTime.now())

            testRunDao.insert(testRun)

            val response = testClient.get("/run/$publicId/summary")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val responseRunSummary = objectMapper.readValue(response.bodyAsText(), TestRunSummary::class.java)
            assertNotNull(responseRunSummary)

            expectThat(responseRunSummary.id).isEqualTo(publicId.id)
            expectThat(responseRunSummary.totalTestCount).isEqualTo(6)
            expectThat(responseRunSummary.totalPassingCount).isEqualTo(4)
            expectThat(responseRunSummary.totalFailureCount).isEqualTo(2)
            expectThat(responseRunSummary.totalSkippedCount).isEqualTo(1)
            expectThat(responseRunSummary.cumulativeDuration).isEqualTo(BigDecimal("30.000"))
            expectThat(responseRunSummary.averageDuration).isEqualTo(BigDecimal("5.000"))
            expectThat(responseRunSummary.slowestTestCaseDuration).isEqualTo(BigDecimal("10.000"))
            expectThat(responseRunSummary.passed).isFalse()
        }
}
