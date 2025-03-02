package projektor.testcase

import projektor.ai.analysis.AITestFailureAnalyzer
import projektor.attachment.AttachmentService
import projektor.server.api.PublicId
import projektor.server.api.TestCase
import projektor.server.api.TestOutput
import projektor.server.api.ai.TestCaseFailureAnalysis

class TestCaseService(
    private val testCaseRepository: TestCaseRepository,
    private val attachmentService: AttachmentService?,
    private val testFailureAnalyzer: AITestFailureAnalyzer?,
) {
    private val attachmentMatchers =
        listOf(
            CypressScreenshotAttachmentMatcher(),
            CypressVideoAttachmentMatcher(),
        )

    suspend fun fetchTestCase(
        testRunPublicId: PublicId,
        testSuiteIdx: Int,
        testCaseIdx: Int,
    ): TestCase? {
        val testCase = testCaseRepository.fetchTestCase(testRunPublicId, testSuiteIdx, testCaseIdx)

        if (testCase?.passed == false) {
            val attachments = attachmentService?.listAttachments(testRunPublicId)

            val testCaseAttachments =
                attachmentMatchers.mapNotNull { attachmentMatcher ->
                    attachmentMatcher.findAttachment(testCase, attachments)
                }

            testCase.attachments = testCaseAttachments
        }

        return testCase
    }

    suspend fun fetchFailedTestCases(publicId: PublicId): List<TestCase> {
        val failedTestCases = testCaseRepository.fetchFailedTestCases(publicId)

        if (failedTestCases.isNotEmpty()) {
            val attachments = attachmentService?.listAttachments(publicId)

            failedTestCases.forEach { testCase ->
                val testCaseAttachments =
                    attachmentMatchers.mapNotNull { attachmentMatcher ->
                        attachmentMatcher.findAttachment(testCase, attachments)
                    }

                testCase.attachments = testCaseAttachments
            }
        }

        return failedTestCases
    }

    suspend fun fetchSlowTestCases(
        publicId: PublicId,
        limit: Int,
    ): List<TestCase> = testCaseRepository.fetchSlowTestCases(publicId, limit)

    suspend fun fetchTestCaseSystemErr(
        publicId: PublicId,
        testSuiteIdx: Int,
        testCaseIdx: Int,
    ): TestOutput = testCaseRepository.fetchTestCaseSystemErr(publicId, testSuiteIdx, testCaseIdx)

    suspend fun fetchTestCaseSystemOut(
        publicId: PublicId,
        testSuiteIdx: Int,
        testCaseIdx: Int,
    ): TestOutput = testCaseRepository.fetchTestCaseSystemOut(publicId, testSuiteIdx, testCaseIdx)

    suspend fun analyzeTestCaseFailure(
        publicId: PublicId,
        testSuiteIdx: Int,
        testCaseIdx: Int,
    ): TestCaseFailureAnalysis? =
        if (testFailureAnalyzer != null) {
            val testCase = testCaseRepository.fetchTestCase(publicId, testSuiteIdx, testCaseIdx)

            val failureTextToAnalyze = testCase?.failure?.failureText ?: testCase?.failure?.failureMessage

            val failureAnalysis = failureTextToAnalyze?.let { testFailureAnalyzer.analyzeTestFailure(it)?.analysis }

            failureAnalysis?.let {
                TestCaseFailureAnalysis(
                    it,
                )
            }
        } else {
            null
        }
}
