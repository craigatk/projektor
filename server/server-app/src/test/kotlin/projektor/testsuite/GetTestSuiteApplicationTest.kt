package projektor.testsuite

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.KtorExperimentalAPI
import kotlin.test.*
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.database.generated.tables.pojos.TestSuiteGroup as TestSuiteGroupDB
import projektor.incomingresults.randomPublicId
import projektor.server.api.TestSuite
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

@KtorExperimentalAPI
class GetTestSuiteApplicationTest : ApplicationTestCase() {
    @Test
    fun shouldFetchTestSuiteFromDatabase() {
        val publicId = randomPublicId()
        val testSuiteIdx = 1

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/suite/$testSuiteIdx") {
                testRunDBGenerator.createTestRun(
                        publicId,
                        listOf(
                                TestSuiteData("testSuite1",
                                        listOf("testSuite1TestCase1"),
                                        listOf("testSuite1TestCase2"),
                                        listOf()
                                ),
                                TestSuiteData("testSuite2",
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
                                TestSuiteData("projektor.TestSuite1",
                                        listOf("testCase1"),
                                        listOf(),
                                        listOf()
                                ),
                                TestSuiteData("projektor.TestSuite2",
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
}
