package projektor.server.api.pullrequest

data class PullRequestFailureContext(
    val testRunPublicId: String,
    val failingTestCases: List<TestCaseFailureContext>,
)

data class TestCaseFailureContext(
    val testName: String,
    val debugContextMarkdown: String,
)
