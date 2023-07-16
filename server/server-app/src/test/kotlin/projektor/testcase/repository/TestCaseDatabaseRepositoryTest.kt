package projektor.testcase.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.TestSuiteData
import projektor.createTestCase
import projektor.createTestFailure
import projektor.createTestRun
import projektor.createTestSuite
import projektor.incomingresults.randomPublicId
import projektor.server.api.TestCase
import projektor.testcase.TestCaseDatabaseRepository
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.containsExactly
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isTrue
import strikt.assertions.map
import java.math.BigDecimal

class TestCaseDatabaseRepositoryTest : DatabaseRepositoryTestCase() {
    @Test
    fun `should fetch failed test cases from multiple test suites`() {
        val publicId = randomPublicId()
        testRunDBGenerator.createTestRun(
            publicId,
            listOf(
                TestSuiteData(
                    "testSuite1",
                    listOf("testSuite1PassedTestCase1", "testSuite1PassedTestCase2"),
                    listOf("testSuite1FailedTestCase1", "testSuite1FailedTestCase2"),
                    listOf()
                ),
                TestSuiteData(
                    "testSuite2",
                    listOf("testSuite2PassedTestCase1", "testSuite2PassedTestCase2"),
                    listOf("testSuite2FailedTestCase1"),
                    listOf()
                )
            )
        )

        val testCaseDatabaseRepository = TestCaseDatabaseRepository(dslContext)

        val failedTestCases = runBlocking { testCaseDatabaseRepository.fetchFailedTestCases(publicId) }

        expectThat(failedTestCases)
            .hasSize(3)
            .map(TestCase::name)
            .contains("testSuite1FailedTestCase1")
            .contains("testSuite1FailedTestCase2")
            .contains("testSuite2FailedTestCase1")
    }

    @Test
    fun `should fetch slowest test cases`() {
        val publicId = randomPublicId()
        val testRun = createTestRun(publicId, 1)
        testRunDao.insert(testRun)

        val testSuite1 = createTestSuite(testRun.id, "ShouldGet1", 1)
        testSuite1.packageName = "com.example"
        testSuiteDao.insert(testSuite1)

        (1..15).forEach { idx ->
            val testSuite1Case = createTestCase(testSuite1.id, "testSuite1Case$idx", idx, true)
            testSuite1Case.duration = BigDecimal.valueOf(10L + idx)
            testCaseDao.insert(testSuite1Case)
        }

        val limit = 5

        val testCaseDatabaseRepository = TestCaseDatabaseRepository(dslContext)

        val slowTestCases = runBlocking { testCaseDatabaseRepository.fetchSlowTestCases(publicId, limit) }

        expectThat(slowTestCases)
            .hasSize(5)
            .map(TestCase::duration)
            .containsExactly(
                BigDecimal("25.000"),
                BigDecimal("24.000"),
                BigDecimal("23.000"),
                BigDecimal("22.000"),
                BigDecimal("21.000")
            )
    }

    @Test
    fun `should fetch test case when it has a failure`() {
        val publicId = randomPublicId()
        val testRun = createTestRun(publicId, 1)
        testRunDao.insert(testRun)

        val testSuite = createTestSuite(testRun.id, "TestSuite", 2)
        testSuite.packageName = "com.example"
        testSuite.hasSystemOut = true
        testSuite.hasSystemErr = false
        testSuiteDao.insert(testSuite)

        val testCaseToFind = createTestCase(testSuite.id, "testCaseToFind", 1, false)
        testCaseDao.insert(testCaseToFind)

        val testCaseFailure = createTestFailure(testCaseToFind.id, "testCaseToFind")
        testFailureDao.insert(testCaseFailure)

        val anotherTestCase = createTestCase(testSuite.id, "anotherTestCase", 2, true)
        testCaseDao.insert(anotherTestCase)

        val testCaseDatabaseRepository = TestCaseDatabaseRepository(dslContext)

        val foundTestCase = runBlocking { testCaseDatabaseRepository.fetchTestCase(publicId, testSuite.idx, 1) }

        expectThat(foundTestCase)
            .isNotNull()
            .and {
                get { idx }.isEqualTo(1)
                get { testSuiteIdx }.isEqualTo(2)
                get { hasSystemOut }.isTrue()
                get { hasSystemErr }.isFalse()
                get { passed }.isFalse()
                get { packageName }.isEqualTo("com.example")
                get { failure }.isNotNull()
                    .and {
                        get { failureMessage }.isEqualTo("testCaseToFind failure message")
                        get { failureText }.isEqualTo("testCaseToFind failure text")
                        get { failureType }.isEqualTo("testCaseToFind failure type")
                    }
            }
    }
}
