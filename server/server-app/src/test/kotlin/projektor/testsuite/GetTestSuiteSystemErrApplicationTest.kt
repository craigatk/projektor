
package projektor.testsuite

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.KtorExperimentalAPI
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.createTestRun
import projektor.createTestSuite
import projektor.incomingresults.randomPublicId
import projektor.server.api.TestOutput
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.assertNotNull

@KtorExperimentalAPI
class GetTestSuiteSystemErrApplicationTest : ApplicationTestCase() {
    @Test
    fun shouldFetchTestSuiteSystemErrFromDatabase() {
        val publicId = randomPublicId()
        val testSuiteIdx = 1
        val systemErr = """Here is some system err
            With multiple
            Lines
        """.trimIndent()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/suite/$testSuiteIdx/systemErr") {
                val testRun = createTestRun(publicId, 1)
                testRunDao.insert(testRun)

                val testSuiteDB1 = createTestSuite(testRun.id, "ShouldFind", 1)
                testSuiteDB1.systemErr = systemErr
                testSuiteDB1.systemOut = "Some system out"
                testSuiteDao.insert(testSuiteDB1)

                val testSuiteDB2 = createTestSuite(testRun.id, "AnotherOne", 2)
                testSuiteDB2.systemErr = "Some other system err"
                testSuiteDB2.systemOut = "Some other output"
                testSuiteDao.insert(testSuiteDB2)
            }.apply {
                val responseOutput = objectMapper.readValue(response.content, TestOutput::class.java)
                assertNotNull(responseOutput)

                expectThat(responseOutput.value).isEqualTo(systemErr)
            }
        }
    }
}
