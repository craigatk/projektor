package projektor.repository.coverage

import projektor.compare.PreviousTestRunService
import projektor.coverage.CoverageService
import projektor.server.api.PublicId
import projektor.server.api.repository.BranchSearch
import projektor.server.api.repository.BranchType
import projektor.server.api.repository.coverage.RepositoryCoverageTimeline
import projektor.server.api.repository.coverage.RepositoryCurrentCoverage
import java.math.BigDecimal
import java.time.Instant

class RepositoryCoverageService(
    private val coverageService: CoverageService,
    private val previousTestRunService: PreviousTestRunService,
    private val repositoryCoverageRepository: RepositoryCoverageRepository,
) {
    suspend fun fetchRepositoryCoverageTimeline(
        branchType: BranchType,
        repoName: String,
        projectName: String?,
    ): RepositoryCoverageTimeline? = repositoryCoverageRepository.fetchRepositoryCoverageTimeline(branchType, repoName, projectName)

    suspend fun fetchRepositoryCurrentCoverage(
        repoName: String,
        projectName: String?,
        branchSearch: BranchSearch?,
    ): RepositoryCurrentCoverage? {
        val mostRecentRunWithCoverage =
            previousTestRunService.findMostRecentRunWithCoverage(
                repoName,
                projectName,
                branchSearch,
            )

        val coveredPercentage = mostRecentRunWithCoverage?.publicId?.let { publicId -> coverageService.getCoveredLinePercentage(publicId) }

        return if (mostRecentRunWithCoverage != null && coveredPercentage != null) {
            RepositoryCurrentCoverage(
                id = mostRecentRunWithCoverage.publicId.id,
                coveredPercentage = coveredPercentage,
                createdTimestamp = mostRecentRunWithCoverage.createdTimestamp,
                repo = repoName,
                branch = mostRecentRunWithCoverage.branch,
                project = projectName,
            )
        } else if (branchSearch == null || branchSearch.branchType == BranchType.MAINLINE) {
            // No live test run with coverage remains (likely cleaned up after its retention period) —
            // fall back to the last known mainline coverage persisted for this repo/project.
            repositoryCoverageRepository.fetchLastKnownCoverage(repoName, projectName)
        } else {
            null
        }
    }

    suspend fun coverageExists(
        repoName: String,
        projectName: String?,
    ): Boolean = repositoryCoverageRepository.coverageExists(repoName, projectName)

    suspend fun saveLastKnownCoverageIfNewer(
        repoName: String,
        projectName: String?,
        branchName: String?,
        coveredPercentage: BigDecimal,
        testRunPublicId: PublicId,
        createdTimestamp: Instant,
    ) = repositoryCoverageRepository.saveLastKnownCoverageIfNewer(
        repoName,
        projectName,
        branchName,
        coveredPercentage,
        testRunPublicId,
        createdTimestamp,
    )
}
