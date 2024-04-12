package projektor.repository.coverage

import projektor.compare.PreviousTestRunService
import projektor.coverage.CoverageService
import projektor.server.api.repository.BranchSearch
import projektor.server.api.repository.BranchType
import projektor.server.api.repository.coverage.RepositoryCoverageTimeline
import projektor.server.api.repository.coverage.RepositoryCurrentCoverage

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
        } else {
            null
        }
    }

    suspend fun coverageExists(
        repoName: String,
        projectName: String?,
    ): Boolean = repositoryCoverageRepository.coverageExists(repoName, projectName)
}
