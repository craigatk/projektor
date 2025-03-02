package projektor.testcase

import io.ktor.client.request.*
import io.ktor.http.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class GetTestCaseAnalysisApplicationTest : ApplicationTestCase() {
    @Test
    fun `when no AI config set should return 204`() =
        projektorTestApplication {
            val publicId = randomPublicId()
            val testSuiteIdx = 1
            val testCaseIdx = 2

            testRunDBGenerator.createTestRun(
                publicId,
                listOf(
                    TestSuiteData(
                        "testSuite1",
                        listOf("testSuite1TestCase1"),
                        listOf("testSuite1TestCase2"),
                        listOf(),
                    ),
                    TestSuiteData(
                        "testSuite2",
                        listOf("testSuite2TestCase1", "testSuite2TestCase2", "testSuite2TestCase3"),
                        listOf(),
                        listOf(),
                    ),
                ),
            )

            val response = client.get("/run/$publicId/suite/$testSuiteIdx/case/$testCaseIdx/analysis")

            expectThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        }
}
