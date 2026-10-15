package projektor.server.api.pullrequest

data class PullRequestFailureContext(
    val testRunPublicId: String,
    val failingTestCases: List<TestCaseFailureContext>,
)

data class TestCaseFailureContext(
    val testName: String,
    // Together with testRunPublicId, these identify the test case for follow-up lookups (e.g. its pass/fail history)
    val testSuiteIdx: Int,
    val testCaseIdx: Int,
    val debugContextMarkdown: String,
)
