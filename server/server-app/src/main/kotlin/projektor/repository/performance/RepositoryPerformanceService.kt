package projektor.repository.performance

import projektor.server.api.repository.performance.RepositoryPerformanceTestTimeline
import projektor.server.api.repository.performance.RepositoryPerformanceTimeline

class RepositoryPerformanceService(private val repositoryPerformanceRepository: RepositoryPerformanceRepository) {
    suspend fun fetchPerformanceTimeline(
        repoName: String,
        projectName: String?,
    ): RepositoryPerformanceTimeline? {
        val entries = repositoryPerformanceRepository.fetchTestTimelineEntries(repoName, projectName)

        return if (entries.isNotEmpty()) {
            val entriesByName = entries.groupBy { it.performanceResult.name }
            val testTimelines: List<RepositoryPerformanceTestTimeline> =
                entriesByName.toList().map { (name, entries) ->
                    RepositoryPerformanceTestTimeline(name, entries)
                }

            RepositoryPerformanceTimeline(testTimelines)
        } else {
            null
        }
    }
}
