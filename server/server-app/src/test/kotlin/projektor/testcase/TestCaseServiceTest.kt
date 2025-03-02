package projektor.testcase

import io.kotest.common.runBlocking
import org.junit.jupiter.api.Test
import org.koin.test.inject
import projektor.DatabaseRepositoryTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

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
}
