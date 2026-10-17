package projektor.repository.performance

import projektor.persistence.objectstore.TestRunIndex
import projektor.server.api.performance.PerformanceResult
import projektor.server.api.repository.performance.RepositoryPerformanceTestTimelineEntry

class RepositoryPerformanceObjectStoreRepository(private val testRunIndex: TestRunIndex) : RepositoryPerformanceRepository {
    override suspend fun fetchTestTimelineEntries(
        repoName: String,
        projectName: String?,
    ): List<RepositoryPerformanceTestTimelineEntry> =
        testRunIndex.readRepositoryEntries(repoName)
            .filter { it.matchesProject(projectName) }
            .flatMap { entry ->
                entry.performanceResults.map { result ->
                    RepositoryPerformanceTestTimelineEntry(
                        publicId = entry.publicId,
                        createdTimestamp = entry.createdTimestamp,
                        performanceResult =
                            PerformanceResult(
                                name = result.name,
                                requestsPerSecond = result.requestsPerSecond,
                                requestCount = result.requestCount,
                                average = result.average,
                                maximum = result.maximum,
                                p95 = result.p95,
                            ),
                    )
                }
            }
}
