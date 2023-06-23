package projektor.testrun

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
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
import kotlin.test.assertNotNull

class GetTestRunApplicationTest : ApplicationTestCase() {

    @Test
    fun `should fetch test run from database`() {
        val publicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId") {
                testRunDBGenerator.createTestRun(
                    publicId,
                    listOf(
                        TestSuiteData(
                            "testSuite1",
                            listOf("testSuite1TestCase1", "testSuite1TestCase2"),
                            listOf(),
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
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val responseRun = objectMapper.readValue(response.content, TestRun::class.java)
                assertNotNull(responseRun)

                expectThat(responseRun.id).isEqualTo(publicId.id)

                val testSuites = responseRun.testSuites
                assertNotNull(testSuites)
                expectThat(testSuites).hasSize(2)

                val testSuite1 = testSuites.find { it.className == "testSuite1" }
                assertNotNull(testSuite1)

                val testSuite2 = testSuites.find { it.className == "testSuite2" }
                assertNotNull(testSuite2)
            }
        }
    }

    @Test
    fun `should fetch test run from database when URL has trailing slash`() {
        val publicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/") {
                testRunDBGenerator.createTestRun(
                    publicId,
                    listOf(
                        TestSuiteData(
                            "testSuite1",
                            listOf("testSuite1TestCase1", "testSuite1TestCase2"),
                            listOf(),
                            listOf()
                        )
                    )
                )
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val responseRun = objectMapper.readValue(response.content, TestRun::class.java)
                assertNotNull(responseRun)

                expectThat(responseRun.id).isEqualTo(publicId.id)

                val testSuites = responseRun.testSuites
                assertNotNull(testSuites)
                expectThat(testSuites).hasSize(1)
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

                val testSuiteGroup1 = TestSuiteGroup()
                testSuiteGroup1.testRunId = testRun.id
                testSuiteGroup1.groupName = "MyGroup1"
                testSuiteGroup1.groupLabel = "MyLabel1"
                testSuiteGroupDao.insert(testSuiteGroup1)

                val testSuiteGroup2 = TestSuiteGroup()
                testSuiteGroup2.testRunId = testRun.id
                testSuiteGroup2.groupName = "MyGroup2"
                testSuiteGroup2.groupLabel = "MyLabel2"
                testSuiteGroupDao.insert(testSuiteGroup2)

                testRunDBGenerator.addTestSuiteGroupToTestRun(testSuiteGroup1, testRun, listOf("TestSuite1"))
                testRunDBGenerator.addTestSuiteGroupToTestRun(testSuiteGroup2, testRun, listOf("TestSuite2"))
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
                        get { groupName }.isEqualTo("MyGroup1")
                        get { groupLabel }.isEqualTo("MyLabel1")
                    }

                val testSuite2 = testSuites.find { it.className == "TestSuite2" }
                expectThat(testSuite2)
                    .isNotNull()
                    .and {
                        get { groupName }.isEqualTo("MyGroup2")
                        get { groupLabel }.isEqualTo("MyLabel2")
                    }
            }
        }
    }
}
