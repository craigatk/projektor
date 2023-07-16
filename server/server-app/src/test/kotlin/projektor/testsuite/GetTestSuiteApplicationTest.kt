package projektor.testsuite

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.ResultsXmlLoader
import projektor.server.api.PublicId
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
    fun `should fetch test suites from database`() {
        val publicId = randomPublicId()
        val testSuiteIdx = 1

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/suite/$testSuiteIdx") {
                testRunDBGenerator.createTestRun(
                    publicId,
                    listOf(
                        TestSuiteData(
                            "testSuite1",
                            listOf("testSuite1TestCase1"),
                            listOf("testSuite1TestCase2"),
                            listOf()
                        ),
                        TestSuiteData(
                            "testSuite2",
                            listOf("testSuite2TestCase1", "testSuite2TestCase2", "testSuite2TestCase3"),
                            listOf(),
                            listOf()
                        )
                    )
                )
            }.apply {
                val responseContent = response.content
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
        }
    }

    @Test
    fun `should fetch grouped test suite from database`() {
        val publicId = randomPublicId()
        val testSuiteIdx = 1

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/suite/$testSuiteIdx") {
                val testRun = testRunDBGenerator.createTestRun(
                    publicId,
                    listOf(
                        TestSuiteData(
                            "projektor.TestSuite1",
                            listOf("testCase1"),
                            listOf(),
                            listOf()
                        ),
                        TestSuiteData(
                            "projektor.TestSuite2",
                            listOf("testCase2"),
                            listOf(),
                            listOf()
                        )
                    )
                )

                val testSuiteGroup = TestSuiteGroupDB()
                testSuiteGroup.testRunId = testRun.id
                testSuiteGroup.groupName = "MyGroup"
                testSuiteGroup.groupLabel = "MyLabel"
                testSuiteGroupDao.insert(testSuiteGroup)

                testRunDBGenerator.addTestSuiteGroupToTestRun(testSuiteGroup, testRun, listOf("TestSuite1", "TestSuite2"))
            }.apply {
                val responseContent = response.content
                assertNotNull(responseContent)

                val responseSuite = objectMapper.readValue(responseContent, TestSuite::class.java)

                expectThat(responseSuite)
                    .isNotNull()
                    .and {
                        get { groupName }.isEqualTo("MyGroup")
                    }
            }
        }
    }

    @Test
    fun `should get test suite with file name`() {
        val resultsBody = GroupedResultsXmlLoader().wrapResultsXmlsInGroup(
            listOf(
                ResultsXmlLoader().cypressWithFilePathAndRootSuiteNameSet()
            )
        )

        val testSuiteIdx = 1

        var publicId: PublicId

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(resultsBody)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                publicId = waitForTestRunSaveToComplete(response).first
            }

            handleRequest(HttpMethod.Get, "/run/$publicId/suite/$testSuiteIdx").apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val responseContent = response.content
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
    }
}
