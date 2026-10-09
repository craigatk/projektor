package projektor.testcase

import projektor.ai.analysis.AITestFailureAnalyzer
import projektor.attachment.AttachmentService
import projektor.server.api.PublicId
import projektor.server.api.TestCase
import projektor.server.api.TestOutput
import projektor.server.api.ai.TestCaseFailureAnalysis
import projektor.server.api.debug.TestCaseDebugContext
import projektor.server.api.failure.FailureClusters
import projektor.server.api.history.TestCaseHistory
import projektor.server.api.slow.SlowTestRegressions
import projektor.testcase.cluster.FailureAnalysisCache
import projektor.testcase.cluster.FailureClusterer
import projektor.testcase.cluster.FailureSignature
import projektor.testcase.slow.SlowTestRegressionDetector

class TestCaseService(
    private val testCaseRepository: TestCaseRepository,
    private val attachmentService: AttachmentService?,
    private val testFailureAnalyzer: AITestFailureAnalyzer?,
    private val failureAnalysisCache: FailureAnalysisCache = FailureAnalysisCache(),
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

    suspend fun fetchSlowTestRegressions(
        publicId: PublicId,
        thresholdPercent: Int,
        baselineRuns: Int,
    ): SlowTestRegressions =
        SlowTestRegressionDetector.detect(
            testCases = testCaseRepository.fetchTestCases(publicId),
            baselines = testCaseRepository.fetchTestCaseDurationBaselines(publicId, baselineRuns),
            thresholdPercent = thresholdPercent,
        )

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

    suspend fun fetchFailureClusters(publicId: PublicId): FailureClusters =
        FailureClusterer.cluster(fetchClusterableFailedTestCases(publicId))

    suspend fun analyzeTestCaseFailure(
        publicId: PublicId,
        testSuiteIdx: Int,
        testCaseIdx: Int,
    ): TestCaseFailureAnalysis? =
        if (testFailureAnalyzer != null) {
            testCaseRepository.fetchTestCase(publicId, testSuiteIdx, testCaseIdx)?.let { testCase ->
                analyzeWithCache(publicId, testCase)
            }
        } else {
            null
        }

    /**
     * Analyzes a single representative failure for the whole cluster. Since the analysis is cached
     * by failure signature, analyzing any other test case in the same cluster reuses this result.
     */
    suspend fun analyzeFailureCluster(
        publicId: PublicId,
        clusterKey: String,
    ): TestCaseFailureAnalysis? =
        if (testFailureAnalyzer != null) {
            val clusterTestCases =
                fetchClusterableFailedTestCases(publicId)
                    .filter { FailureSignature.fromTestCase(it).key == clusterKey }

            clusterTestCases.firstOrNull()?.let { representative -> analyzeWithCache(publicId, representative) }
        } else {
            null
        }

    private suspend fun fetchClusterableFailedTestCases(publicId: PublicId): List<TestCase> =
        testCaseRepository.fetchFailedTestCases(publicId).filterNot { it.skipped }

    private suspend fun analyzeWithCache(
        publicId: PublicId,
        testCase: TestCase,
    ): TestCaseFailureAnalysis? {
        val failureTextToAnalyze = testCase.failure?.failureText ?: testCase.failure?.failureMessage ?: return null

        val failureAnalysis =
            failureAnalysisCache.getOrAnalyze(publicId, FailureSignature.fromTestCase(testCase).key) {
                testFailureAnalyzer?.analyzeTestFailure(failureTextToAnalyze)
            }

        return failureAnalysis?.let { TestCaseFailureAnalysis(it.analysis) }
    }

    suspend fun buildTestCaseDebugContext(
        publicId: PublicId,
        testSuiteIdx: Int,
        testCaseIdx: Int,
    ): TestCaseDebugContext? {
        val testCase = testCaseRepository.fetchTestCase(publicId, testSuiteIdx, testCaseIdx) ?: return null

        val systemOut =
            if (testCase.hasSystemOutTestCase) {
                testCaseRepository.fetchTestCaseSystemOut(publicId, testSuiteIdx, testCaseIdx).value
            } else {
                null
            }

        val systemErr =
            if (testCase.hasSystemErrTestCase) {
                testCaseRepository.fetchTestCaseSystemErr(publicId, testSuiteIdx, testCaseIdx).value
            } else {
                null
            }

        return TestCaseDebugContext(buildTestCaseDebugContextMarkdown(testCase, systemOut, systemErr))
    }

    suspend fun fetchTestCaseHistory(
        publicId: PublicId,
        testSuiteIdx: Int,
        testCaseIdx: Int,
        maxRuns: Int,
    ): TestCaseHistory = buildTestCaseHistory(testCaseRepository.fetchTestCaseHistory(publicId, testSuiteIdx, testCaseIdx, maxRuns))
}
