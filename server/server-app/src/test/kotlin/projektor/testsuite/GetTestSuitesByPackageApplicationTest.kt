
package projektor.testsuite

import com.fasterxml.jackson.core.type.TypeReference
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.createTestRun
import projektor.createTestSuite
import projektor.incomingresults.randomPublicId
import projektor.server.api.TestSuite
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.map

class GetTestSuitesByPackageApplicationTest : ApplicationTestCase() {
    @Test
    fun `should get test suites by package`() =
        projektorTestApplication {
            val publicId = randomPublicId()

            val testRun = createTestRun(publicId, 1)
            testRunDao.insert(testRun)

            val testSuiteDB1 = createTestSuite(testRun.id, "ShouldGet1", 1)
            testSuiteDB1.packageName = "com.example"
            testSuiteDao.insert(testSuiteDB1)

            val testSuiteDB2 = createTestSuite(testRun.id, "ShouldGet2", 2)
            testSuiteDB2.packageName = "com.example"
            testSuiteDao.insert(testSuiteDB2)

            val testSuiteDB3 = createTestSuite(testRun.id, "SubPackageShouldNotGet", 3)
            testSuiteDB3.packageName = "com.example.extra"
            testSuiteDao.insert(testSuiteDB3)

            val testSuiteDB4 = createTestSuite(testRun.id, "DifferentPackageShouldNotGet", 4)
            testSuiteDB4.packageName = "com.something.else"
            testSuiteDao.insert(testSuiteDB4)

            val response = client.get("/run/$publicId/suites?package=com.example")

            val responseSuites: List<TestSuite> =
                objectMapper.readValue(
                    response.bodyAsText(),
                    object : TypeReference<List<TestSuite>>() {},
                )

            expectThat(responseSuites)
                .hasSize(2)
                .map(TestSuite::className)
                .contains("ShouldGet1", "ShouldGet2")
        }
}
