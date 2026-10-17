package projektor.testrun.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.parser.model.Failure
import projektor.parser.model.Skipped
import projektor.persistence.DatabaseTestBackend
import projektor.persistence.ObjectStoreTestBackend
import projektor.persistence.RepositoryTestCase
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.assertNotNull
import projektor.parser.model.TestCase as ParsedTestCase
import projektor.parser.model.TestSuite as ParsedTestSuite

abstract class TestRunRepositoryFetchRunTest : RepositoryTestCase() {
    @Test
    fun `should fetch all fields in test run and test suite`() {
        val publicId = randomPublicId()

        val testCases =
            listOf(
                ParsedTestCase().apply {
                    name = "should pass"
                    className = "projektor.TestSuiteSpec"
                    time = BigDecimal("5.00")
                },
                ParsedTestCase().apply {
                    name = "should fail"
                    className = "projektor.TestSuiteSpec"
                    time = BigDecimal("4.00")
                    failure = Failure().apply { message = "Failed" }
                },
                ParsedTestCase().apply {
                    name = "should skip"
                    className = "projektor.TestSuiteSpec"
                    time = BigDecimal("0.00")
                    skipped = Skipped()
                },
            )

        val testSuite =
            ParsedTestSuite().apply {
                name = "projektor.TestSuiteSpec"
                tests = 3
                failures = 1
                skipped = 1
                timestamp = LocalDateTime.of(2019, 11, 1, 0, 0, 0)
                hostname = "myhostname"
                time = BigDecimal("9.00")
                systemOut = "Some system out"
                this.testCases = testCases
            }

        runBlocking { testRunRepository.saveTestRun(publicId, listOf(testSuite)) }

        val testRun = runBlocking { testRunRepository.fetchTestRun(publicId) }
        assertNotNull(testRun)

        expectThat(testRun)
            .get { summary }.and {
                get { totalTestCount }.isEqualTo(3)
                get { totalPassingCount }.isEqualTo(1)
                get { totalFailureCount }.isEqualTo(1)
                get { totalSkippedCount }.isEqualTo(1)
                get { cumulativeDuration }.isEqualTo(BigDecimal("9.000"))
                get { averageDuration }.isEqualTo(BigDecimal("3.000"))
                get { slowestTestCaseDuration }.isEqualTo(BigDecimal("5.000"))
            }

        expectThat(testRun.testSuites)
            .isNotNull()
            .hasSize(1)
            .any {
                get { idx }.isEqualTo(1)
                get { testCount }.isEqualTo(3)
                get { passingCount }.isEqualTo(1)
                get { failureCount }.isEqualTo(1)
                get { skippedCount }.isEqualTo(1)
                get { className }.isEqualTo("TestSuiteSpec")
                get { packageName }.isNotNull().isEqualTo("projektor")
                get { startTs }.isNotNull()
                get { hostname }.isNotNull().isEqualTo("myhostname")
                get { duration }.isEqualTo(BigDecimal("9.000"))
                get { hasSystemOut }.isEqualTo(true)
                get { hasSystemErr }.isEqualTo(false)
            }
    }

    @Test
    fun `should fetch test run with multiple test suites`() {
        val publicId = randomPublicId()

        testRunGenerator.createTestRun(
            publicId,
            listOf(
                TestSuiteData(
                    "testSuite1",
                    listOf("testSuite1PassedTestCase1", "testSuitePassed1TestCase2"),
                    listOf("testSuite1FailedTestCase1"),
                    listOf("testSuite1SkippedTestCase1", "testSuite1SkippedTestCase2"),
                ),
                TestSuiteData(
                    "testSuite2",
                    listOf("testSuite2PassedTestCase1"),
                    listOf("testSuite2FailedTestCase1", "testSuite2FailedTestCase2", "testSuite2FailedTestCase3"),
                    listOf(),
                ),
            ),
        )

        val testRun = runBlocking { testRunRepository.fetchTestRun(publicId) }
        assertNotNull(testRun)

        expectThat(testRun) {
            get { testSuites }
                .isNotNull()
                .and {
                    any {
                        get { className }.isEqualTo("testSuite1")
                    }
                    any {
                        get { className }.isEqualTo("testSuite2")
                    }
                }
        }

        val testSuite1 = testRun.testSuites?.find { it.className == "testSuite1" }
        assertNotNull(testSuite1)

        expectThat(testSuite1) {
            get { testCount }.isEqualTo(5)
            get { passingCount }.isEqualTo(2)
            get { failureCount }.isEqualTo(1)
            get { skippedCount }.isEqualTo(2)
        }
    }
}

class TestRunRepositoryFetchRunDatabaseTest : TestRunRepositoryFetchRunTest() {
    override fun createBackend() = DatabaseTestBackend()
}

class TestRunRepositoryFetchRunObjectStoreTest : TestRunRepositoryFetchRunTest() {
    override fun createBackend() = ObjectStoreTestBackend()
}
