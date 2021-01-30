package projektor.repository.testrun

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.*
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.server.api.repository.RepositoryFlakyTests
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import kotlin.test.assertNotNull

@KtorExperimentalAPI
class RepositoryFlakyTestApplicationTest : ApplicationTestCase() {

    @Test
    fun `when flaky tests without a project name should return them`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = null

        val publicIds = (1..5).map { randomPublicId() }

        val testSuiteDataList = listOf(
            TestSuiteData(
                "projektor.failingTestSuite1",
                listOf("passing1"),
                listOf("failing1"),
                listOf()
            ),
            TestSuiteData(
                "projektor.failingTestSuite2",
                listOf("passing2"),
                listOf("failing2"),
                listOf()
            )
        )

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/tests/flaky") {
                publicIds.forEach { publicId ->
                    testRunDBGenerator.createTestRunInRepo(publicId, testSuiteDataList, repoName, true, projectName)
                }
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val flakyTests = objectMapper.readValue(response.content, RepositoryFlakyTests::class.java)
                assertNotNull(flakyTests)

                expectThat(flakyTests.tests).hasSize(2).and {
                    any { get { testCase }.get { name }.isEqualTo("failing1") }
                    any { get { testCase }.get { name }.isEqualTo("failing2") }
                }
            }
        }
    }

    @Test
    fun `when flaky tests within specified max runs and threshold should find them`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = null

        val failingPublicIds = (1..3).map { randomPublicId() }
        val failingTestSuiteDataList = listOf(
            TestSuiteData(
                "projektor.failingTestSuite1",
                listOf("passing1"),
                listOf("failing1"),
                listOf()
            ),
            TestSuiteData(
                "projektor.failingTestSuite2",
                listOf("passing2"),
                listOf("failing2"),
                listOf()
            )
        )

        val passingPublicIds = (1..7).map { randomPublicId() }
        val passingTestSuiteDataList = listOf(
            TestSuiteData(
                "projektor.passingTestSuite",
                listOf("passing1", "passing2"),
                listOf(),
                listOf()
            )
        )

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/tests/flaky?max_runs=10&threshold=3") {
                failingPublicIds.forEach { publicId ->
                    testRunDBGenerator.createTestRunInRepo(publicId, failingTestSuiteDataList, repoName, true, projectName)
                }
                passingPublicIds.forEach { publicId ->
                    testRunDBGenerator.createTestRunInRepo(publicId, passingTestSuiteDataList, repoName, true, projectName)
                }
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val flakyTests = objectMapper.readValue(response.content, RepositoryFlakyTests::class.java)
                assertNotNull(flakyTests)

                expectThat(flakyTests.tests).hasSize(2)
            }
        }
    }

    @Test
    fun `when flaky tests are beyond specified max runs and threshold should return 204`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = null

        val failingPublicIds = (1..3).map { randomPublicId() }
        val failingTestSuiteDataList = listOf(
            TestSuiteData(
                "projektor.failingTestSuite1",
                listOf("passing1"),
                listOf("failing1"),
                listOf()
            ),
            TestSuiteData(
                "projektor.failingTestSuite2",
                listOf("passing2"),
                listOf("failing2"),
                listOf()
            )
        )

        val passingPublicIds = (1..8).map { randomPublicId() }
        val passingTestSuiteDataList = listOf(
            TestSuiteData(
                "projektor.passingTestSuite",
                listOf("passing1", "passing2"),
                listOf(),
                listOf()
            )
        )

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/tests/flaky?max_runs=10&threshold=3") {
                failingPublicIds.forEach { publicId ->
                    testRunDBGenerator.createTestRunInRepo(publicId, failingTestSuiteDataList, repoName, true, projectName)
                }
                passingPublicIds.forEach { publicId ->
                    testRunDBGenerator.createTestRunInRepo(publicId, passingTestSuiteDataList, repoName, true, projectName)
                }
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.NoContent)
            }
        }
    }

    @Test
    fun `when no flaky tests without a project name should return 204`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = null

        val publicIds = (1..5).map { randomPublicId() }

        val testSuiteDataList = listOf(
            TestSuiteData(
                "projektor.passingTestSuite",
                listOf("passing"),
                listOf(),
                listOf()
            )
        )

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/tests/flaky") {
                publicIds.forEach { publicId ->
                    testRunDBGenerator.createTestRunInRepo(publicId, testSuiteDataList, repoName, true, projectName)
                }
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.NoContent)
            }
        }
    }

    @Test
    fun `when flaky tests with a project name should return them`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = "my-project"

        val publicIds = (1..5).map { randomPublicId() }

        val testSuiteDataList = listOf(
            TestSuiteData(
                "projektor.failingTestSuite1",
                listOf("passing1"),
                listOf("failing1"),
                listOf()
            ),
            TestSuiteData(
                "projektor.failingTestSuite2",
                listOf("passing2"),
                listOf("failing2"),
                listOf()
            )
        )

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/project/$projectName/tests/flaky") {
                publicIds.forEach { publicId ->
                    testRunDBGenerator.createTestRunInRepo(publicId, testSuiteDataList, repoName, true, projectName)
                }
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val flakyTests = objectMapper.readValue(response.content, RepositoryFlakyTests::class.java)
                assertNotNull(flakyTests)

                expectThat(flakyTests.tests).hasSize(2).and {
                    any { get { testCase }.get { name }.isEqualTo("failing1") }
                    any { get { testCase }.get { name }.isEqualTo("failing2") }
                }
            }
        }
    }

    @Test
    fun `when no flaky tests with a project name should return 204`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = "my-project"

        val publicIds = (1..5).map { randomPublicId() }

        val testSuiteDataList = listOf(
            TestSuiteData(
                "projektor.passingTestSuite",
                listOf("passing"),
                listOf(),
                listOf()
            )
        )

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/project/$projectName/tests/flaky") {
                publicIds.forEach { publicId ->
                    testRunDBGenerator.createTestRunInRepo(publicId, testSuiteDataList, repoName, true, projectName)
                }
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.NoContent)
            }
        }
    }
}
