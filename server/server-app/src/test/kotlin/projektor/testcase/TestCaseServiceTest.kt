package projektor.testcase

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.test.inject
import projektor.DatabaseRepositoryTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

class TestCaseServiceTest : DatabaseRepositoryTestCase() {
    init {
        aiTestFailureAnalyzerEnabled = true
    }

    @Test
    fun `should get test failure analysis`() {
        val testCaseService: TestCaseService by inject()

        val publicId = randomPublicId()

        testRunDBGenerator.createTestRun(
            publicId,
            listOf(
                TestSuiteData(
                    "testSuite1",
                    listOf(),
                    listOf("failingTestSuite1TestCase1"),
                    listOf(),
                ),
            ),
        )

        val analysis = runBlocking { testCaseService.analyzeTestCaseFailure(publicId, 1, 1) }
        expectThat(analysis?.analysis).isNotNull().isEqualTo("The test assertion failed due to a setup error")
    }

    @Test
    fun `should build test case debug context markdown`() {
        val testCaseService: TestCaseService by inject()

        val publicId = randomPublicId()

        testRunDBGenerator.createTestRun(
            publicId,
            listOf(
                TestSuiteData(
                    "testSuite1",
                    listOf(),
                    listOf("failingTestSuite1TestCase1"),
                    listOf(),
                ),
            ),
        )

        val debugContext = runBlocking { testCaseService.buildTestCaseDebugContext(publicId, 1, 1) }

        expectThat(debugContext?.markdown).isNotNull()
        expectThat(debugContext!!.markdown).contains("failingTestSuite1TestCase1 failure message")
        expectThat(debugContext.markdown).contains("failingTestSuite1TestCase1 failure text")
        expectThat(debugContext.markdown).contains("failingTestSuite1TestCase1 failure type")
    }

    @Test
    fun `should return null test case debug context when test case not found`() {
        val testCaseService: TestCaseService by inject()

        val publicId = randomPublicId()

        val debugContext = runBlocking { testCaseService.buildTestCaseDebugContext(publicId, 1, 1) }

        expectThat(debugContext).isNull()
    }
}
