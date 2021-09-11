package projektor.badge

import projektor.compare.PreviousTestRunService
import projektor.coverage.CoverageService
import projektor.notification.badge.SvgCoverageBadgeCreator
import projektor.server.api.PublicId
import projektor.server.api.repository.BranchType

class CoverageBadgeService(
    private val coverageService: CoverageService,
    private val previousTestRunService: PreviousTestRunService,
    private val svgCoverageBadgeCreator: SvgCoverageBadgeCreator
) {
    suspend fun createCoverageBadge(fullRepoName: String, projectName: String?): String? {

        val mostRecentRunWithCoverage = previousTestRunService.findMostRecentRunWithCoverage(
            BranchType.MAINLINE,
            fullRepoName,
            projectName
        ) ?: previousTestRunService.findMostRecentRunWithCoverage(
            BranchType.ALL,
            fullRepoName,
            projectName
        )

        val coveredPercentage = mostRecentRunWithCoverage?.publicId?.let { publicId ->
            coverageService.getCoveredLinePercentage(publicId)
        }

        return coveredPercentage?.let { svgCoverageBadgeCreator.createBadge(it) }
    }

    suspend fun createCoverageBadge(publicId: PublicId): String? {
        val coveredPercentage = coverageService.getCoveredLinePercentage(publicId)

        return coveredPercentage?.let { svgCoverageBadgeCreator.createBadge(it) }
    }
}
