package projektor.pullrequest

import projektor.coverage.CoverageService
import projektor.coverage.GroupedCoverageFile
import projektor.coverage.fullPath
import projektor.repository.testrun.RepositoryTestRunRepository
import projektor.server.api.PublicId
import projektor.server.api.pullrequest.CoverageFileDelta
import projektor.server.api.pullrequest.PullRequestCoverageDelta

class PullRequestCoverageService(
    private val repositoryTestRunRepository: RepositoryTestRunRepository,
    private val coverageService: CoverageService,
) {
    suspend fun findMostRecentTestRunWithCoverage(
        orgName: String,
        repoName: String,
        pullRequestNumber: Int,
    ): PublicId? = repositoryTestRunRepository.fetchMostRecentTestRunPublicIdWithCoverage("$orgName/$repoName", pullRequestNumber)

    suspend fun fetchCoverageDelta(
        orgName: String,
        repoName: String,
        pullRequestNumber: Int,
        maxChangedFiles: Int,
    ): PullRequestCoverageDelta? {
        val publicId = findMostRecentTestRunWithCoverage(orgName, repoName, pullRequestNumber) ?: return null

        val coverage = coverageService.getCoverageWithPreviousRunComparison(publicId) ?: return null

        val currentFiles = coverageService.getAllCoverageFiles(publicId)
        val baselineFiles =
            coverage.previousTestRunId
                ?.let { coverageService.getAllCoverageFiles(PublicId(it)) }
                ?.associateBy { it.key() }
                .orEmpty()

        val changedFiles =
            currentFiles.mapNotNull { current ->
                val currentPercentage = current.coverageFile.stats.lineStat.coveredPercentage
                val baseline = baselineFiles[current.key()]
                val delta = baseline?.let { currentPercentage - it.coverageFile.stats.lineStat.coveredPercentage }

                if (baseline != null && delta?.signum() == 0) {
                    null
                } else {
                    CoverageFileDelta(
                        groupName = current.groupName,
                        filePath = current.coverageFile.fullPath(),
                        lineCoveredPercentage = currentPercentage,
                        lineCoveredPercentageDelta = delta,
                        missedLines = current.coverageFile.missedLines.toList(),
                        partialLines = current.coverageFile.partialLines.toList(),
                    )
                }
            }.sortedWith(
                // Drops first (biggest first), then files new since the baseline (least covered first), then increases
                compareBy<CoverageFileDelta> {
                    val delta = it.lineCoveredPercentageDelta
                    when {
                        delta == null -> 1
                        delta.signum() < 0 -> 0
                        else -> 2
                    }
                }.thenBy { it.lineCoveredPercentageDelta ?: it.lineCoveredPercentage },
            )

        return PullRequestCoverageDelta(
            testRunPublicId = publicId.id,
            baselineTestRunPublicId = coverage.previousTestRunId,
            overallStats = coverage.overallStats,
            groups = coverage.groups,
            changedFiles = changedFiles.take(maxChangedFiles),
            changedFilesTruncated = changedFiles.size > maxChangedFiles,
        )
    }

    private fun GroupedCoverageFile.key() = groupName to path
}
