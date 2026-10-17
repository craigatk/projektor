package projektor.testcase.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.persistence.DatabaseTestBackend
import projektor.persistence.ObjectStoreTestBackend
import projektor.persistence.RepositoryTestCase
import projektor.persistence.TestRunGenerator
import projektor.server.api.TestCase
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

abstract class TestCaseRepositoryTest : RepositoryTestCase() {
    @Test
    fun `should fetch failed test cases from multiple test suites`() {
        val publicId = randomPublicId()
        testRunGenerator.createTestRun(
            publicId,
            listOf(
                TestSuiteData(
                    "testSuite1",
                    listOf("testSuite1PassedTestCase1", "testSuite1PassedTestCase2"),
                    listOf("testSuite1FailedTestCase1", "testSuite1FailedTestCase2"),
                    listOf(),
                ),
                TestSuiteData(
                    "testSuite2",
                    listOf("testSuite2PassedTestCase1", "testSuite2PassedTestCase2"),
                    listOf("testSuite2FailedTestCase1"),
                    listOf(),
                ),
            ),
        )

        val failedTestCases = runBlocking { testCaseRepository.fetchFailedTestCases(publicId) }

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
        val testSuite =
            TestRunGenerator.toParsedTestSuite(
                TestSuiteData("com.example.ShouldGet1", (1..15).map { "testSuite1Case$it" }, listOf(), listOf()),
            )
        testSuite.testCases.forEachIndexed { idx, testCase -> testCase.time = BigDecimal.valueOf(11L + idx) }

        runBlocking { testRunRepository.saveTestRun(publicId, listOf(testSuite)) }

        val limit = 5

        val slowTestCases = runBlocking { testCaseRepository.fetchSlowTestCases(publicId, limit) }

        expectThat(slowTestCases)
            .hasSize(5)
            .map(TestCase::duration)
            .containsExactly(
                BigDecimal("25.000"),
                BigDecimal("24.000"),
                BigDecimal("23.000"),
                BigDecimal("22.000"),
                BigDecimal("21.000"),
            )
    }

    @Test
    fun `should fetch test case when it has a failure`() {
        val publicId = randomPublicId()
        val testSuite =
            TestRunGenerator.toParsedTestSuite(
                TestSuiteData("com.example.TestSuite", listOf("anotherTestCase"), listOf("testCaseToFind"), listOf()),
            )
        // Put the test case to find first in its test suite
        testSuite.testCases = testSuite.testCases.reversed()
        testSuite.systemOut = "test suite system out"

        val otherTestSuite =
            TestRunGenerator.toParsedTestSuite(
                TestSuiteData("com.example.OtherTestSuite", listOf("otherTestCase"), listOf(), listOf()),
            )

        runBlocking { testRunRepository.saveTestRun(publicId, listOf(otherTestSuite, testSuite)) }

        val foundTestCase = runBlocking { testCaseRepository.fetchTestCase(publicId, 2, 1) }

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

class TestCaseRepositoryDatabaseTest : TestCaseRepositoryTest() {
    override fun createBackend() = DatabaseTestBackend()
}

class TestCaseRepositoryObjectStoreTest : TestCaseRepositoryTest() {
    override fun createBackend() = ObjectStoreTestBackend()
}
