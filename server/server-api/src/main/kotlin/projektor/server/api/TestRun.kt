package projektor.server.api

data class TestRun(val id: String, val summary: TestRunSummary, val testSuites: List<TestSuite>?)
