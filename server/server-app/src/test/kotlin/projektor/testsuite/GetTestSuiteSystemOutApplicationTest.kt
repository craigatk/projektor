
package projektor.testsuite

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.test.dispatcher.*
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
    override fun autoStartServer(): Boolean = true

    @Test
    fun shouldFetchTestSuiteSystemOutFromDatabase() =
        testSuspend {
            val publicId = randomPublicId()
            val testSuiteIdx = 1
            val systemOut =
                """
                Here is some system output
                With multiple
                Lines
                """.trimIndent()

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

            val response = testClient.get("/run/$publicId/suite/$testSuiteIdx/systemOut")

            val responseOutput = objectMapper.readValue(response.bodyAsText(), TestOutput::class.java)
            assertNotNull(responseOutput)

            expectThat(responseOutput.value).isEqualTo(systemOut)
        }
}
