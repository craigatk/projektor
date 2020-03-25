package projektor.testrun.repository

import java.math.BigDecimal
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlinx.coroutines.runBlocking
import projektor.DatabaseRepositoryTestCase
import projektor.TestSuiteData
import projektor.database.generated.tables.pojos.TestCase
import projektor.database.generated.tables.pojos.TestRun
import projektor.database.generated.tables.pojos.TestSuite
import projektor.incomingresults.randomPublicId
import projektor.testrun.TestRunDatabaseRepository
import strikt.api.expectThat
import strikt.assertions.*

class TestRunDatabaseRepositoryFetchRunTest : DatabaseRepositoryTestCase() {

    @Test
    fun `should fetch all fields in test run, test suite, and test case`() {
        val testRunDatabaseRepository = TestRunDatabaseRepository(dslContext)
        val publicId = randomPublicId()

        val testRunDB = TestRun()
        testRunDB.publicId = publicId.id
        testRunDB.totalTestCount = 3
        testRunDB.totalPassingCount = 1
        testRunDB.totalFailureCount = 1
        testRunDB.totalSkippedCount = 1
        testRunDB.passed = false
        testRunDB.cumulativeDuration = BigDecimal("9.00")
        testRunDB.averageDuration = BigDecimal("3.00")
        testRunDB.slowestTestCaseDuration = BigDecimal("5.00")
        testRunDB.createdTimestamp = Timestamp.from(Instant.now())
        testRunDao.insert(testRunDB)

        val testSuiteDB = TestSuite()
        testSuiteDB.testRunId = testRunDB.id
        testSuiteDB.idx = 1
        testSuiteDB.testCount = 3
        testSuiteDB.passingCount = 1
        testSuiteDB.skippedCount = 1
        testSuiteDB.failureCount = 1
        testSuiteDB.className = "TestSuiteSpec"
        testSuiteDB.packageName = "projektor"
        testSuiteDB.startTs = Timestamp.valueOf(LocalDateTime.of(2019, 11, 1, 0, 0, 0))
        testSuiteDB.hostname = "myhostname"
        testSuiteDB.duration = BigDecimal("9.00")
        testSuiteDB.systemOut = "Some system out"
        testSuiteDB.systemErr = null
        testSuiteDB.hasSystemOut = true
        testSuiteDB.hasSystemErr = false
        testSuiteDao.insert(testSuiteDB)

        val passingTestCaseDB = TestCase()
        passingTestCaseDB.testSuiteId = testSuiteDB.id
        passingTestCaseDB.idx = 1
        passingTestCaseDB.name = "should pass"
        passingTestCaseDB.className = "TestSuiteSpec"
        passingTestCaseDB.packageName = "projektor"
        passingTestCaseDB.duration = BigDecimal("5.00")
        passingTestCaseDB.passed = true
        passingTestCaseDB.skipped = false
        testCaseDao.insert(passingTestCaseDB)

        val failingTestCaseDB = TestCase()
        failingTestCaseDB.testSuiteId = testSuiteDB.id
        failingTestCaseDB.idx = 2
        failingTestCaseDB.name = "should fail"
        failingTestCaseDB.className = "TestSuiteSpec"
        failingTestCaseDB.packageName = "projektor"
        failingTestCaseDB.duration = BigDecimal("4.00")
        failingTestCaseDB.passed = false
        failingTestCaseDB.skipped = false
        testCaseDao.insert(failingTestCaseDB)

        val skippedTestCaseDB = TestCase()
        skippedTestCaseDB.testSuiteId = testSuiteDB.id
        skippedTestCaseDB.idx = 3
        skippedTestCaseDB.name = "should skip"
        skippedTestCaseDB.className = "TestSuiteSpec"
        skippedTestCaseDB.packageName = "projektor"
        skippedTestCaseDB.duration = BigDecimal("0.00")
        skippedTestCaseDB.passed = false
        skippedTestCaseDB.skipped = true
        testCaseDao.insert(skippedTestCaseDB)

        val testRun = runBlocking { testRunDatabaseRepository.fetchTestRun(publicId) }
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

        val testCases = testRun.testSuites?.first()?.testCases
        assertNotNull(testCases)

        expectThat(testCases)
                .hasSize(3)
                .any {
                    get { idx }.isEqualTo(1)
                    get { testSuiteIdx }.isEqualTo(1)
                    get { name }.isEqualTo("should pass")
                    get { className }.isEqualTo("TestSuiteSpec")
                    get { packageName }.isNotNull().isEqualTo("projektor")
                    get { duration }.isEqualTo(BigDecimal("5.000"))
                    get { passed }.isTrue()
                    get { skipped }.isFalse()
                }
                .any {
                    get { idx }.isEqualTo(2)
                    get { testSuiteIdx }.isEqualTo(1)
                    get { name }.isEqualTo("should fail")
                    get { className }.isEqualTo("TestSuiteSpec")
                    get { packageName }.isNotNull().isEqualTo("projektor")
                    get { duration }.isEqualTo(BigDecimal("4.000"))
                    get { passed }.isFalse()
                    get { skipped }.isFalse()
                }
                .any {
                    get { idx }.isEqualTo(3)
                    get { testSuiteIdx }.isEqualTo(1)
                    get { name }.isEqualTo("should skip")
                    get { className }.isEqualTo("TestSuiteSpec")
                    get { packageName }.isNotNull().isEqualTo("projektor")
                    get { duration }.isEqualTo(BigDecimal("0.000"))
                    get { passed }.isFalse()
                    get { skipped }.isTrue()
                }
    }

    @Test
    fun `should fetch test run with multiple test suites`() {
        val testRunDatabaseRepository = TestRunDatabaseRepository(dslContext)
        val publicId = randomPublicId()

        testRunDBGenerator.createTestRun(
                publicId,
                listOf(
                        TestSuiteData(
                                "testSuite1",
                                listOf("testSuite1PassedTestCase1", "testSuitePassed1TestCase2"),
                                listOf("testSuite1FailedTestCase1"),
                                listOf("testSuite1SkippedTestCase1", "testSuite1SkippedTestCase2")
                        ),
                        TestSuiteData(
                                "testSuite2",
                                listOf("testSuite2PassedTestCase1"),
                                listOf("testSuite2FailedTestCase1", "testSuite2FailedTestCase2", "testSuite2FailedTestCase3"),
                                listOf()
                        )
                )
        )

        val testRun = runBlocking { testRunDatabaseRepository.fetchTestRun(publicId) }
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

            val testSuite1TestCases = testSuite1.testCases
            assertNotNull(testSuite1TestCases)

            expectThat(testSuite1TestCases).any {
                get { name }.isEqualTo("testSuite1PassedTestCase1")
                get { passed }.isTrue()
                get { skipped }.isFalse()
            }

            expectThat(testSuite1TestCases).any {
                get { name }.isEqualTo("testSuitePassed1TestCase2")
                get { passed }.isTrue()
                get { skipped }.isFalse()
            }

            expectThat(testSuite1TestCases).any {
                get { name }.isEqualTo("testSuite1FailedTestCase1")
                get { passed }.isFalse()
                get { skipped }.isFalse()
            }

            expectThat(testSuite1TestCases).any {
                get { name }.isEqualTo("testSuite1SkippedTestCase1")
                get { passed }.isFalse()
                get { skipped }.isTrue()
            }

            expectThat(testSuite1TestCases).any {
                get { name }.isEqualTo("testSuite1SkippedTestCase2")
                get { passed }.isFalse()
                get { skipped }.isTrue()
            }
        }
    }
}
