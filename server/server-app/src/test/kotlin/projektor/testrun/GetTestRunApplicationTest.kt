package projektor.testrun

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.KtorExperimentalAPI
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.database.generated.tables.pojos.TestSuiteGroup
import projektor.incomingresults.randomPublicId
import projektor.server.api.TestRun
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

@KtorExperimentalAPI
class GetTestRunApplicationTest : ApplicationTestCase() {
    @Test
    fun `should fetch test run from database`() {
        val publicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId") {
                testRunDBGenerator.createTestRun(
                        publicId,
                        listOf(
                                TestSuiteData("testSuite1",
                                        listOf("testSuite1TestCase1", "testSuite1TestCase2"),
                                        listOf(),
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
                val responseRun = objectMapper.readValue(response.content, TestRun::class.java)
                assertNotNull(responseRun)

                expectThat(responseRun.id).isEqualTo(publicId.id)

                val testSuites = responseRun.testSuites
                assertNotNull(testSuites)
                expectThat(testSuites).hasSize(2)

                val testSuite1 = testSuites.find { it.className == "testSuite1" }
                assertNotNull(testSuite1)

                val testSuite1TestCases = testSuite1.testCases
                assertNotNull(testSuite1TestCases)
                expectThat(testSuite1TestCases).hasSize(2)

                val testSuite2 = testSuites.find { it.className == "testSuite2" }
                assertNotNull(testSuite2)

                val testSuite2TestCases = testSuite2.testCases
                assertNotNull(testSuite2TestCases)
                expectThat(testSuite2TestCases).hasSize(3)
            }
        }
    }

    @Test
    fun `should fetch grouped test run from database`() {
        val publicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId") {
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

                val testSuiteGroup = TestSuiteGroup()
                testSuiteGroup.testRunId = testRun.id
                testSuiteGroup.groupName = "MyGroup"
                testSuiteGroup.groupLabel = "MyLabel"
                testSuiteGroupDao.insert(testSuiteGroup)

                testRunDBGenerator.addTestSuiteGroupToTestRun(testSuiteGroup, testRun, listOf("TestSuite1", "TestSuite2"))
            }.apply {
                val responseRun = objectMapper.readValue(response.content, TestRun::class.java)
                assertNotNull(responseRun)

                expectThat(responseRun.id).isEqualTo(publicId.id)

                val testSuites = responseRun.testSuites
                assertNotNull(testSuites)
                expectThat(testSuites).hasSize(2)

                val testSuite1 = testSuites.find { it.className == "TestSuite1" }
                expectThat(testSuite1)
                        .isNotNull()
                        .and {
                            get { groupName }.isEqualTo("MyGroup")
                            get { groupLabel }.isEqualTo("MyLabel")
                        }

                val testSuite2 = testSuites.find { it.className == "TestSuite2" }
                expectThat(testSuite2)
                        .isNotNull()
                        .and {
                            get { groupName }.isEqualTo("MyGroup")
                            get { groupLabel }.isEqualTo("MyLabel")
                        }
            }
        }
    }
}
