package projektor.testrun.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.incomingresults.randomPublicId
import projektor.parser.model.Failure
import projektor.testrun.TestRunDatabaseRepository
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import kotlin.test.assertNotNull
import projektor.parser.model.TestCase as ParsedTestCase
import projektor.parser.model.TestSuite as ParsedTestSuite

class TestRunDatabaseRepositorySaveResultsTest : DatabaseRepositoryTestCase() {

    @Test
    fun `should save test run with two passing test cases`() {
        val testRunDatabaseRepository = TestRunDatabaseRepository(dslContext)

        val publicId = randomPublicId()

        val passingTestSuite = ParsedTestSuite()
        passingTestSuite.name = "PassingSuite"
        passingTestSuite.tests = 2
        passingTestSuite.skipped = 0
        passingTestSuite.failures = 0
        passingTestSuite.errors = 0

        val passingTestCase1 = ParsedTestCase()
        passingTestCase1.name = "PassingTestCase1"
        passingTestCase1.className = "PassingTestCase1Class"

        val passingTestCase2 = ParsedTestCase()
        passingTestCase2.name = "PassingTestCase2"
        passingTestCase2.className = "PassingTestCase2Class"

        passingTestSuite.testCases = listOf(passingTestCase1, passingTestCase2)

        runBlocking { testRunDatabaseRepository.saveTestRun(publicId, listOf(passingTestSuite)) }

        val testRunDB = testRunDao.fetchOneByPublicId(publicId.id)
        assertNotNull(testRunDB)

        val testSuiteDBs = testSuiteDao.fetchByTestRunId(testRunDB.id)
        expectThat(testSuiteDBs).hasSize(1)
        val testSuite = testSuiteDBs[0]
        expectThat(testSuite.className).isEqualTo("PassingSuite")
        expectThat(testSuite.testCount).isEqualTo(2)
        expectThat(testSuite.passingCount).isEqualTo(2)
        expectThat(testSuite.skippedCount).isEqualTo(0)
        expectThat(testSuite.failureCount).isEqualTo(0)

        val testCases = testCaseDao.fetchByTestSuiteId(testSuite.id)
        expectThat(testCases).hasSize(2)

        expectThat(testCases).any {
            get { name }.isEqualTo("PassingTestCase1")
            get { className }.isEqualTo("PassingTestCase1Class")
        }

        expectThat(testCases).any {
            get { name }.isEqualTo("PassingTestCase2")
            get { className }.isEqualTo("PassingTestCase2Class")
        }
    }

    @Test
    fun `should save test run with a failed test case and an errored test case`() {
        val testRunDatabaseRepository = TestRunDatabaseRepository(dslContext)

        val publicId = randomPublicId()

        val failingTestSuite = ParsedTestSuite()
        failingTestSuite.name = "FailingSuite"
        failingTestSuite.tests = 2
        failingTestSuite.skipped = 0
        failingTestSuite.failures = 1
        failingTestSuite.errors = 1

        val failedTestCase = ParsedTestCase()
        failedTestCase.name = "FailedTestCase"
        failedTestCase.className = "FailedTestCaseClass"
        val failedTestCaseFailure = Failure()
        failedTestCaseFailure.message = "Failed test case"
        failedTestCase.failure = failedTestCaseFailure

        val erroredTestCase = ParsedTestCase()
        erroredTestCase.name = "ErroredTestCase"
        erroredTestCase.className = "ErroredTestCaseClass"
        val erroredTestCaseFailed = Failure()
        erroredTestCaseFailed.message = "Errored test case"
        erroredTestCase.failure = erroredTestCaseFailed

        failingTestSuite.testCases = listOf(failedTestCase, erroredTestCase)

        runBlocking { testRunDatabaseRepository.saveTestRun(publicId, listOf(failingTestSuite)) }

        val testRunDB = testRunDao.fetchOneByPublicId(publicId.id)
        assertNotNull(testRunDB)

        val testSuiteDBs = testSuiteDao.fetchByTestRunId(testRunDB.id)
        expectThat(testSuiteDBs).hasSize(1)
        val testSuite = testSuiteDBs[0]
        expectThat(testSuite.className).isEqualTo("FailingSuite")
        expectThat(testSuite.testCount).isEqualTo(2)
        expectThat(testSuite.passingCount).isEqualTo(0)
        expectThat(testSuite.skippedCount).isEqualTo(0)
        expectThat(testSuite.failureCount).isEqualTo(2)

        val testCases = testCaseDao.fetchByTestSuiteId(testSuite.id)
        expectThat(testCases).hasSize(2)

        expectThat(testCases).any {
            get { name }.isEqualTo("FailedTestCase")
            get { className }.isEqualTo("FailedTestCaseClass")
        }

        val failedTestCaseDB = testCases.find { it.name == "FailedTestCase" }
        assertNotNull(failedTestCaseDB)
        val failedTestCaseFailuresDB = testFailureDao.fetchByTestCaseId(failedTestCaseDB.id)
        expectThat(failedTestCaseFailuresDB)
            .hasSize(1)
            .any {
                get { failureMessage }.isEqualTo("Failed test case")
            }

        expectThat(testCases).any {
            get { name }.isEqualTo("ErroredTestCase")
            get { className }.isEqualTo("ErroredTestCaseClass")
        }

        val erroredTestCaseDB = testCases.find { it.name == "ErroredTestCase" }
        assertNotNull(erroredTestCaseDB)
        val erroredTestCaseFailuresDB = testFailureDao.fetchByTestCaseId(erroredTestCaseDB.id)
        expectThat(erroredTestCaseFailuresDB)
            .hasSize(1)
            .any {
                get { failureMessage }.isEqualTo("Errored test case")
            }
    }
}
