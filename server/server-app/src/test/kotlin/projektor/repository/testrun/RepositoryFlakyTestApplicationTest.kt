package projektor.repository.testrun

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import io.ktor.test.dispatcher.*
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.server.api.repository.RepositoryFlakyTests
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import kotlin.test.assertNotNull

class RepositoryFlakyTestApplicationTest : ApplicationTestCase() {
    override fun autoStartServer(): Boolean = true

    @Test
    fun `when flaky tests without a project name should return them`() =
        testSuspend {
            val orgName = RandomStringUtils.randomAlphabetic(12)
            val repoName = "$orgName/repo"
            val projectName = null

            val publicIds = (1..5).map { randomPublicId() }

            val testSuiteDataList =
                listOf(
                    TestSuiteData(
                        "projektor.failingTestSuite1",
                        listOf("passing1"),
                        listOf("failing1"),
                        listOf(),
                    ),
                    TestSuiteData(
                        "projektor.failingTestSuite2",
                        listOf("passing2"),
                        listOf("failing2"),
                        listOf(),
                    ),
                )

            publicIds.forEach { publicId ->
                testRunDBGenerator.createTestRunInRepo(publicId, testSuiteDataList, repoName, true, projectName)
            }

            val response = testClient.get("/repo/$repoName/tests/flaky")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val flakyTests = objectMapper.readValue(response.bodyAsText(), RepositoryFlakyTests::class.java)
            assertNotNull(flakyTests)

            expectThat(flakyTests.tests).hasSize(2).and {
                any { get { testCase }.get { name }.isEqualTo("failing1") }
                any { get { testCase }.get { name }.isEqualTo("failing2") }
            }
        }

    @Test
    fun `when flaky tests within specified max runs and threshold should find them`() =
        testSuspend {
            val orgName = RandomStringUtils.randomAlphabetic(12)
            val repoName = "$orgName/repo"
            val projectName = null

            val failingPublicIds = (1..3).map { randomPublicId() }
            val failingTestSuiteDataList =
                listOf(
                    TestSuiteData(
                        "projektor.failingTestSuite1",
                        listOf("passing1"),
                        listOf("failing1"),
                        listOf(),
                    ),
                    TestSuiteData(
                        "projektor.failingTestSuite2",
                        listOf("passing2"),
                        listOf("failing2"),
                        listOf(),
                    ),
                )

            val passingPublicIds = (1..7).map { randomPublicId() }
            val passingTestSuiteDataList =
                listOf(
                    TestSuiteData(
                        "projektor.passingTestSuite",
                        listOf("passing1", "passing2"),
                        listOf(),
                        listOf(),
                    ),
                )

            failingPublicIds.forEach { publicId ->
                testRunDBGenerator.createTestRunInRepo(publicId, failingTestSuiteDataList, repoName, true, projectName)
            }
            passingPublicIds.forEach { publicId ->
                testRunDBGenerator.createTestRunInRepo(publicId, passingTestSuiteDataList, repoName, true, projectName)
            }

            val response = testClient.get("/repo/$repoName/tests/flaky?max_runs=10&threshold=3")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val flakyTests = objectMapper.readValue(response.bodyAsText(), RepositoryFlakyTests::class.java)
            assertNotNull(flakyTests)

            expectThat(flakyTests.tests).hasSize(2)
        }

    @Test
    fun `when flaky tests are beyond specified max runs and threshold should return 204`() =
        testSuspend {
            val orgName = RandomStringUtils.randomAlphabetic(12)
            val repoName = "$orgName/repo"
            val projectName = null

            val failingPublicIds = (1..3).map { randomPublicId() }
            val failingTestSuiteDataList =
                listOf(
                    TestSuiteData(
                        "projektor.failingTestSuite1",
                        listOf("passing1"),
                        listOf("failing1"),
                        listOf(),
                    ),
                    TestSuiteData(
                        "projektor.failingTestSuite2",
                        listOf("passing2"),
                        listOf("failing2"),
                        listOf(),
                    ),
                )

            val passingPublicIds = (1..8).map { randomPublicId() }
            val passingTestSuiteDataList =
                listOf(
                    TestSuiteData(
                        "projektor.passingTestSuite",
                        listOf("passing1", "passing2"),
                        listOf(),
                        listOf(),
                    ),
                )

            failingPublicIds.forEach { publicId ->
                testRunDBGenerator.createTestRunInRepo(publicId, failingTestSuiteDataList, repoName, true, projectName)
            }
            passingPublicIds.forEach { publicId ->
                testRunDBGenerator.createTestRunInRepo(publicId, passingTestSuiteDataList, repoName, true, projectName)
            }

            val response = testClient.get("/repo/$repoName/tests/flaky?max_runs=10&threshold=3")

            expectThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        }

    @Test
    fun `when no flaky tests without a project name should return 204`() =
        testSuspend {
            val orgName = RandomStringUtils.randomAlphabetic(12)
            val repoName = "$orgName/repo"
            val projectName = null

            val publicIds = (1..5).map { randomPublicId() }

            val testSuiteDataList =
                listOf(
                    TestSuiteData(
                        "projektor.passingTestSuite",
                        listOf("passing"),
                        listOf(),
                        listOf(),
                    ),
                )

            publicIds.forEach { publicId ->
                testRunDBGenerator.createTestRunInRepo(publicId, testSuiteDataList, repoName, true, projectName)
            }

            val response = testClient.get("/repo/$repoName/tests/flaky")

            expectThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        }

    @Test
    fun `when flaky tests with a project name should return them`() =
        testSuspend {
            val orgName = RandomStringUtils.randomAlphabetic(12)
            val repoName = "$orgName/repo"
            val projectName = "my-project"

            val publicIds = (1..5).map { randomPublicId() }

            val testSuiteDataList =
                listOf(
                    TestSuiteData(
                        "projektor.failingTestSuite1",
                        listOf("passing1"),
                        listOf("failing1"),
                        listOf(),
                    ),
                    TestSuiteData(
                        "projektor.failingTestSuite2",
                        listOf("passing2"),
                        listOf("failing2"),
                        listOf(),
                    ),
                )

            publicIds.forEach { publicId ->
                testRunDBGenerator.createTestRunInRepo(publicId, testSuiteDataList, repoName, true, projectName)
            }

            val response = testClient.get("/repo/$repoName/project/$projectName/tests/flaky")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val flakyTests = objectMapper.readValue(response.bodyAsText(), RepositoryFlakyTests::class.java)
            assertNotNull(flakyTests)

            expectThat(flakyTests.tests).hasSize(2).and {
                any { get { testCase }.get { name }.isEqualTo("failing1") }
                any { get { testCase }.get { name }.isEqualTo("failing2") }
            }
        }

    @Test
    fun `should find flaky tests in mainline only`() =
        testSuspend {
            val orgName = RandomStringUtils.randomAlphabetic(12)
            val repoName = "$orgName/repo"
            val projectName = null

            val failingMainlinePublicIds = (1..3).map { randomPublicId() }
            val failingMainlineTestSuiteDataList =
                listOf(
                    TestSuiteData(
                        "projektor.failingMainlineTestSuite1",
                        listOf("passing1"),
                        listOf("failingMainline1"),
                        listOf(),
                    ),
                    TestSuiteData(
                        "projektor.failingMainlineTestSuite2",
                        listOf("passing2"),
                        listOf("failingMainline2"),
                        listOf(),
                    ),
                )

            val failingBranchPublicIds = (1..3).map { randomPublicId() }
            val failingBranchTestSuiteDataList =
                listOf(
                    TestSuiteData(
                        "projektor.failingBranchTestSuite1",
                        listOf("passing1"),
                        listOf("failingBranch1"),
                        listOf(),
                    ),
                    TestSuiteData(
                        "projektor.failingBranchTestSuite2",
                        listOf("passing2"),
                        listOf("failingBranch2"),
                        listOf(),
                    ),
                )

            val passingPublicIds = (1..7).map { randomPublicId() }
            val passingTestSuiteDataList =
                listOf(
                    TestSuiteData(
                        "projektor.passingTestSuite",
                        listOf("passing1", "passing2"),
                        listOf(),
                        listOf(),
                    ),
                )

            failingMainlinePublicIds.forEach { publicId ->
                testRunDBGenerator.createTestRunInRepo(publicId, failingMainlineTestSuiteDataList, repoName, true, projectName, "main")
            }
            failingBranchPublicIds.forEach { publicId ->
                testRunDBGenerator.createTestRunInRepo(
                    publicId,
                    failingBranchTestSuiteDataList,
                    repoName,
                    true,
                    projectName,
                    "my-branch",
                )
            }
            passingPublicIds.forEach { publicId ->
                testRunDBGenerator.createTestRunInRepo(publicId, passingTestSuiteDataList, repoName, true, projectName)
            }

            val response = testClient.get("/repo/$repoName/tests/flaky?max_runs=20&threshold=3&branch_type=MAINLINE")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val flakyTests = objectMapper.readValue(response.bodyAsText(), RepositoryFlakyTests::class.java)
            assertNotNull(flakyTests)

            expectThat(flakyTests.tests).hasSize(2)

            val flakyTestCaseNames = flakyTests.tests.map { it.testCase }.map { it.fullName }

            expectThat(flakyTestCaseNames)
                .contains(
                    "projektor.failingMainline1ClassName.failingMainline1",
                    "projektor.failingMainline2ClassName.failingMainline2",
                )
                .not().contains(
                    "projektor.failingBranch1ClassName.failingBranch1",
                    "projektor.failingBranch2ClassName.failingBranch2",
                )
        }

    @Test
    fun `should find flaky tests in all branches`() =
        testSuspend {
            val orgName = RandomStringUtils.randomAlphabetic(12)
            val repoName = "$orgName/repo"
            val projectName = null

            val failingMainlinePublicIds = (1..3).map { randomPublicId() }
            val failingMainlineTestSuiteDataList =
                listOf(
                    TestSuiteData(
                        "projektor.failingMainlineTestSuite1",
                        listOf("passing1"),
                        listOf("failingMainline1"),
                        listOf(),
                    ),
                    TestSuiteData(
                        "projektor.failingMainlineTestSuite2",
                        listOf("passing2"),
                        listOf("failingMainline2"),
                        listOf(),
                    ),
                )

            val failingBranchPublicIds = (1..3).map { randomPublicId() }
            val failingBranchTestSuiteDataList =
                listOf(
                    TestSuiteData(
                        "projektor.failingBranchTestSuite1",
                        listOf("passing1"),
                        listOf("failingBranch1"),
                        listOf(),
                    ),
                    TestSuiteData(
                        "projektor.failingBranchTestSuite2",
                        listOf("passing2"),
                        listOf("failingBranch2"),
                        listOf(),
                    ),
                )

            val passingPublicIds = (1..7).map { randomPublicId() }
            val passingTestSuiteDataList =
                listOf(
                    TestSuiteData(
                        "projektor.passingTestSuite",
                        listOf("passing1", "passing2"),
                        listOf(),
                        listOf(),
                    ),
                )

            failingMainlinePublicIds.forEach { publicId ->
                testRunDBGenerator.createTestRunInRepo(publicId, failingMainlineTestSuiteDataList, repoName, true, projectName, "main")
            }
            failingBranchPublicIds.forEach { publicId ->
                testRunDBGenerator.createTestRunInRepo(
                    publicId,
                    failingBranchTestSuiteDataList,
                    repoName,
                    true,
                    projectName,
                    "my-branch",
                )
            }
            passingPublicIds.forEach { publicId ->
                testRunDBGenerator.createTestRunInRepo(publicId, passingTestSuiteDataList, repoName, true, projectName)
            }

            val response = testClient.get("/repo/$repoName/tests/flaky?max_runs=20&threshold=3&branch_type=ALL")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val flakyTests = objectMapper.readValue(response.bodyAsText(), RepositoryFlakyTests::class.java)
            assertNotNull(flakyTests)

            expectThat(flakyTests.tests).hasSize(4)

            val flakyTestCaseNames = flakyTests.tests.map { it.testCase }.map { it.fullName }

            expectThat(flakyTestCaseNames)
                .contains(
                    "projektor.failingMainline1ClassName.failingMainline1",
                    "projektor.failingMainline2ClassName.failingMainline2",
                )
                .contains(
                    "projektor.failingBranch1ClassName.failingBranch1",
                    "projektor.failingBranch2ClassName.failingBranch2",
                )
        }

    @Test
    fun `when no flaky tests with a project name should return 204`() =
        testSuspend {
            val orgName = RandomStringUtils.randomAlphabetic(12)
            val repoName = "$orgName/repo"
            val projectName = "my-project"

            val publicIds = (1..5).map { randomPublicId() }

            val testSuiteDataList =
                listOf(
                    TestSuiteData(
                        "projektor.passingTestSuite",
                        listOf("passing"),
                        listOf(),
                        listOf(),
                    ),
                )

            publicIds.forEach { publicId ->
                testRunDBGenerator.createTestRunInRepo(publicId, testSuiteDataList, repoName, true, projectName)
            }

            val response = testClient.get("/repo/$repoName/project/$projectName/tests/flaky")

            expectThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        }
}
