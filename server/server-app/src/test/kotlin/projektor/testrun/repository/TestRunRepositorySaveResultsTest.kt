package projektor.testrun.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.incomingresults.randomPublicId
import projektor.parser.model.Failure
import projektor.persistence.DatabaseTestBackend
import projektor.persistence.ObjectStoreTestBackend
import projektor.persistence.RepositoryTestCase
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.math.BigDecimal
import projektor.parser.model.TestCase as ParsedTestCase
import projektor.parser.model.TestSuite as ParsedTestSuite

abstract class TestRunRepositorySaveResultsTest : RepositoryTestCase() {
    @Test
    fun `should save test run with two passing test cases`() {
        val publicId = randomPublicId()

        val passingTestSuite = ParsedTestSuite()
        passingTestSuite.name = "PassingSuite"
        passingTestSuite.tests = 2
        passingTestSuite.skipped = 0
        passingTestSuite.failures = 0
        passingTestSuite.errors = 0
        // The database repository can't read back a test suite without a duration, see OBJECT_STORE_PLAN.md
        passingTestSuite.time = BigDecimal("1.5")

        val passingTestCase1 = ParsedTestCase()
        passingTestCase1.name = "PassingTestCase1"
        passingTestCase1.className = "PassingTestCase1Class"

        val passingTestCase2 = ParsedTestCase()
        passingTestCase2.name = "PassingTestCase2"
        passingTestCase2.className = "PassingTestCase2Class"

        passingTestSuite.testCases = listOf(passingTestCase1, passingTestCase2)

        runBlocking { testRunRepository.saveTestRun(publicId, listOf(passingTestSuite)) }

        expectThat(runBlocking { testRunRepository.fetchTestRun(publicId) }).isNotNull()

        val testSuites = runBlocking { testSuiteRepository.fetchTestSuitesWithCases(publicId) }
        expectThat(testSuites).hasSize(1)
        val testSuite = testSuites[0]
        expectThat(testSuite.className).isEqualTo("PassingSuite")
        expectThat(testSuite.testCount).isEqualTo(2)
        expectThat(testSuite.passingCount).isEqualTo(2)
        expectThat(testSuite.skippedCount).isEqualTo(0)
        expectThat(testSuite.failureCount).isEqualTo(0)

        val testCases = testSuite.testCases.orEmpty()
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
        val publicId = randomPublicId()

        val failingTestSuite = ParsedTestSuite()
        failingTestSuite.name = "FailingSuite"
        failingTestSuite.tests = 2
        failingTestSuite.skipped = 0
        failingTestSuite.failures = 1
        failingTestSuite.errors = 1
        // The database repository can't read back a test suite without a duration, see OBJECT_STORE_PLAN.md
        failingTestSuite.time = BigDecimal("1.5")

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

        runBlocking { testRunRepository.saveTestRun(publicId, listOf(failingTestSuite)) }

        expectThat(runBlocking { testRunRepository.fetchTestRun(publicId) }).isNotNull()

        val testSuites = runBlocking { testSuiteRepository.fetchTestSuitesWithCases(publicId) }
        expectThat(testSuites).hasSize(1)
        val testSuite = testSuites[0]
        expectThat(testSuite.className).isEqualTo("FailingSuite")
        expectThat(testSuite.testCount).isEqualTo(2)
        expectThat(testSuite.passingCount).isEqualTo(0)
        expectThat(testSuite.skippedCount).isEqualTo(0)
        expectThat(testSuite.failureCount).isEqualTo(2)

        val testCases = testSuite.testCases.orEmpty()
        expectThat(testCases).hasSize(2)

        expectThat(testCases).any {
            get { name }.isEqualTo("FailedTestCase")
            get { className }.isEqualTo("FailedTestCaseClass")
        }

        val savedFailedTestCase = testCases.find { it.name == "FailedTestCase" }
        expectThat(savedFailedTestCase?.failure?.failureMessage).isEqualTo("Failed test case")

        expectThat(testCases).any {
            get { name }.isEqualTo("ErroredTestCase")
            get { className }.isEqualTo("ErroredTestCaseClass")
        }

        val savedErroredTestCase = testCases.find { it.name == "ErroredTestCase" }
        expectThat(savedErroredTestCase?.failure?.failureMessage).isEqualTo("Errored test case")
    }
}

class TestRunRepositorySaveResultsDatabaseTest : TestRunRepositorySaveResultsTest() {
    override fun createBackend() = DatabaseTestBackend()
}

class TestRunRepositorySaveResultsObjectStoreTest : TestRunRepositorySaveResultsTest() {
    override fun createBackend() = ObjectStoreTestBackend()
}
