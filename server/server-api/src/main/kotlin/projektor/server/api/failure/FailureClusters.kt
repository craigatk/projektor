package projektor.server.api.failure

data class FailureClusters(
    val totalFailedTestCount: Int,
    val clusters: List<FailureCluster>,
)

data class FailureCluster(
    val key: String,
    val title: String,
    val failureType: String?,
    val normalizedMessage: String?,
    val location: String?,
    val testCaseCount: Int,
    val representative: FailureClusterTestCase,
    val testCases: List<FailureClusterTestCase>,
)

data class FailureClusterTestCase(
    val testSuiteIdx: Int,
    val testCaseIdx: Int,
    val fullName: String,
)
