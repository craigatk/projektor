package projektor.ai.analysis

class MockAITestFailureAnalyzer : AITestFailureAnalyzer {
    override suspend fun analyzeTestFailure(testOutput: String): TestFailureAnalysis? {
        return TestFailureAnalysis(analysis = "The test assertion failed due to a setup error", 1)
    }
}
