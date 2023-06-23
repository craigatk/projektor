
package projektor.testsuite

import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.createTestRun
import projektor.createTestSuite
import projektor.incomingresults.randomPublicId
import projektor.server.api.TestOutput
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.assertNotNull

class GetTestSuiteSystemOutApplicationTest : ApplicationTestCase() {
    @Test
    fun shouldFetchTestSuiteSystemOutFromDatabase() {
        val publicId = randomPublicId()
        val testSuiteIdx = 1
        val systemOut = """Here is some system output
            With multiple
            Lines
        """.trimIndent()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/suite/$testSuiteIdx/systemOut") {
                val testRun = createTestRun(publicId, 1)
                testRunDao.insert(testRun)

                val testSuiteDB1 = createTestSuite(testRun.id, "ShouldFind", 1)
                testSuiteDB1.systemOut = systemOut
                testSuiteDB1.systemErr = "Some system err"
                testSuiteDao.insert(testSuiteDB1)

                val testSuiteDB2 = createTestSuite(testRun.id, "AnotherOne", 2)
                testSuiteDB2.systemOut = "Some other output"
                testSuiteDB2.systemErr = "Some other system err"
                testSuiteDao.insert(testSuiteDB2)
            }.apply {
                val responseOutput = objectMapper.readValue(response.content, TestOutput::class.java)
                assertNotNull(responseOutput)

                expectThat(responseOutput.value).isEqualTo(systemOut)
            }
        }
    }
}
