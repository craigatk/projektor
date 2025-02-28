package projektor.ai.analysis

interface AITestFailureAnalyzer {
    suspend fun analyzeTestFailure(testOutput: String): TestFailureAnalysis?
}
