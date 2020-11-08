package projektor.repository.performance

import projektor.server.api.repository.performance.RepositoryPerformanceTestTimelineEntry

interface RepositoryPerformanceRepository {
    suspend fun fetchTestTimelineEntries(repoName: String, projectName: String?): List<RepositoryPerformanceTestTimelineEntry>
}
