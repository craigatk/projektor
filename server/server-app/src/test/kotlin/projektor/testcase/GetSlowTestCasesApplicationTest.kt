package projektor.testcase

import com.fasterxml.jackson.core.type.TypeReference
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.createTestCase
import projektor.createTestRun
import projektor.createTestSuite
import projektor.incomingresults.randomPublicId
import projektor.server.api.TestCase
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.map
import java.math.BigDecimal

class GetSlowTestCasesApplicationTest : ApplicationTestCase() {
    @Test
    fun `should fetch slow test cases from database`() {
        val publicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/${publicId.id}/cases/slow") {
                val testRun = createTestRun(publicId, 1)
                testRunDao.insert(testRun)

                val testSuite1 = createTestSuite(testRun.id, "ShouldGet1", 1)
                testSuite1.packageName = "com.example"
                testSuiteDao.insert(testSuite1)

                (1..15).forEach { idx ->
                    val testSuite1Case = createTestCase(testSuite1.id, "testSuite1Case$idx", idx, true)
                    testSuite1Case.duration = BigDecimal.valueOf(10L + idx)
                    testCaseDao.insert(testSuite1Case)
                }
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val slowTestCases: List<TestCase> = objectMapper.readValue(response.content, object : TypeReference<List<TestCase>>() {})

                expectThat(slowTestCases)
                    .hasSize(10)
                    .map(TestCase::duration)
                    .containsExactly(
                        BigDecimal("25.000"),
                        BigDecimal("24.000"),
                        BigDecimal("23.000"),
                        BigDecimal("22.000"),
                        BigDecimal("21.000"),
                        BigDecimal("20.000"),
                        BigDecimal("19.000"),
                        BigDecimal("18.000"),
                        BigDecimal("17.000"),
                        BigDecimal("16.000")
                    )
            }
        }
    }
}
