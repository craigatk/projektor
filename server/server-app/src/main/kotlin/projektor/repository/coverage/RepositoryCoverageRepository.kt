package projektor.repository.coverage

import projektor.server.api.PublicId
import projektor.server.api.repository.BranchType
import projektor.server.api.repository.coverage.RepositoryCoverageTimeline
import projektor.server.api.repository.coverage.RepositoryCurrentCoverage
import java.math.BigDecimal
import java.time.Instant

interface RepositoryCoverageRepository {
    suspend fun fetchRepositoryCoverageTimeline(
        branchType: BranchType,
        repoName: String,
        projectName: String?,
    ): RepositoryCoverageTimeline?

    suspend fun coverageExists(
        repoName: String,
        projectName: String?,
    ): Boolean

    // Persists the last known mainline coverage for a repo/project so it survives test run cleanup.
    // Only updates the stored value if createdTimestamp is newer than what's already saved.
    suspend fun saveLastKnownCoverageIfNewer(
        repoName: String,
        projectName: String?,
        branchName: String?,
        coveredPercentage: BigDecimal,
        testRunPublicId: PublicId,
        createdTimestamp: Instant,
    )

    suspend fun fetchLastKnownCoverage(
        repoName: String,
        projectName: String?,
    ): RepositoryCurrentCoverage?
}
