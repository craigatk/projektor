package projektor.server.api.repository.performance

data class RepositoryPerformanceTestTimeline(
    val name: String,
    val entries: List<RepositoryPerformanceTestTimelineEntry>,
)
