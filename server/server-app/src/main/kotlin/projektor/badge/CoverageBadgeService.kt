package projektor.badge

import projektor.compare.PreviousTestRunService
import projektor.coverage.CoverageService
import projektor.notification.badge.SvgCoverageBadgeCreator
import projektor.server.api.PublicId

class CoverageBadgeService(
    private val coverageService: CoverageService,
    private val previousTestRunService: PreviousTestRunService,
    private val svgCoverageBadgeCreator: SvgCoverageBadgeCreator
) {
    suspend fun createCoverageBadge(fullRepoName: String, projectName: String?): String? {

        val mostRecentMainBranchRunWithCoverage = previousTestRunService.findMostRecentMainBranchRunWithCoverage(fullRepoName, projectName)

        val coveredPercentage = mostRecentMainBranchRunWithCoverage?.publicId?.let { publicId ->
            coverageService.getCoveredLinePercentage(publicId)
        }

        return coveredPercentage?.let { svgCoverageBadgeCreator.createBadge(it) }
    }

    suspend fun createCoverageBadge(publicId: PublicId): String? {
        val coveredPercentage = coverageService.getCoveredLinePercentage(publicId)

        return coveredPercentage?.let { svgCoverageBadgeCreator.createBadge(it) }
    }
}
