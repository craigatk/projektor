package projektor.server.api.coverage

data class Coverage(
    val groups: List<CoverageGroup>,
    val overallStats: CoverageStats,
    val previousTestRunId: String?
) {
    fun findCoverageGroup(name: String): CoverageGroup? = groups.find { it.name == name }
}
