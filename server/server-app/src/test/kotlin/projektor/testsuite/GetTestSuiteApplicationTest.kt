package projektor.testsuite

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.ResultsXmlLoader
import projektor.server.api.TestSuite
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import kotlin.test.assertNotNull
import projektor.database.generated.tables.pojos.TestSuiteGroup as TestSuiteGroupDB

class GetTestSuiteApplicationTest : ApplicationTestCase() {
    @Test
    fun `should fetch test suites from database`() =
        projektorTestApplication {
            val publicId = randomPublicId()
            val testSuiteIdx = 1

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

            val response = client.get("/run/$publicId/suite/$testSuiteIdx")

            val responseContent = response.bodyAsText()
            assertNotNull(responseContent)

            val responseSuite = objectMapper.readValue(responseContent, TestSuite::class.java)
            assertNotNull(responseSuite)

            expectThat(responseSuite.fileName).isNull()

            val testCases = responseSuite.testCases
            assertNotNull(testCases)

            expectThat(testCases).hasSize(2)

            val testCase1 = testCases.find { it.name == "testSuite1TestCase1" }
            assertNotNull(testCase1)
            expectThat(testCase1.testSuiteIdx).isEqualTo(testSuiteIdx)

            val testCase2 = testCases.find { it.name == "testSuite1TestCase2" }
            assertNotNull(testCase2)
            expectThat(testCase2.testSuiteIdx).isEqualTo(testSuiteIdx)

            val testCase1Failure = testCase2.failure
            assertNotNull(testCase1Failure)
        }

    @Test
    fun `should fetch grouped test suite from database`() =
        projektorTestApplication {
            val publicId = randomPublicId()
            val testSuiteIdx = 1

            val testRun =
                testRunDBGenerator.createTestRun(
                    publicId,
                    listOf(
                        TestSuiteData(
                            "projektor.TestSuite1",
                            listOf("testCase1"),
                            listOf(),
                            listOf(),
                        ),
                        TestSuiteData(
                            "projektor.TestSuite2",
                            listOf("testCase2"),
                            listOf(),
                            listOf(),
                        ),
                    ),
                )

            val testSuiteGroup = TestSuiteGroupDB()
            testSuiteGroup.testRunId = testRun.id
            testSuiteGroup.groupName = "MyGroup"
            testSuiteGroup.groupLabel = "MyLabel"
            testSuiteGroupDao.insert(testSuiteGroup)

            testRunDBGenerator.addTestSuiteGroupToTestRun(testSuiteGroup, testRun, listOf("TestSuite1", "TestSuite2"))

            val response = client.get("/run/$publicId/suite/$testSuiteIdx")

            val responseContent = response.bodyAsText()
            assertNotNull(responseContent)

            val responseSuite = objectMapper.readValue(responseContent, TestSuite::class.java)

            expectThat(responseSuite)
                .isNotNull()
                .and {
                    get { groupName }.isEqualTo("MyGroup")
                }
        }

    @Test
    fun `should get test suite with file name`() =
        projektorTestApplication {
            val resultsBody =
                GroupedResultsXmlLoader().wrapResultsXmlsInGroup(
                    listOf(
                        ResultsXmlLoader().cypressWithFilePathAndRootSuiteNameSet(),
                    ),
                )

            val testSuiteIdx = 1

            val postResponse = client.postGroupedResultsJSON(resultsBody)
            val publicId = waitForTestRunSaveToComplete(postResponse).first

            val getResponse = client.get("/run/$publicId/suite/$testSuiteIdx")

            expectThat(getResponse.status).isEqualTo(HttpStatusCode.OK)

            val responseContent = getResponse.bodyAsText()
            assertNotNull(responseContent)

            val responseSuite = objectMapper.readValue(responseContent, TestSuite::class.java)

            expectThat(responseSuite)
                .isNotNull()
                .and {
                    get { fileName }.isEqualTo("cypress/integration/repository_timeline.spec.js")
                    get { className }.isEqualTo("repository coverage suite")
                }
        }
}
