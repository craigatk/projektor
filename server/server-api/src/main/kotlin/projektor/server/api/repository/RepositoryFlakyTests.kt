package projektor.server.api.repository

data class RepositoryFlakyTests(
    val tests: List<RepositoryFlakyTest>,
    val maxRuns: Int,
    val failureCountThreshold: Int,
)
