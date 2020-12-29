package projektor.badge

import projektor.compare.PreviousTestRunService
import projektor.coverage.CoverageService
import projektor.notification.badge.SvgCoverageBadgeCreator

class RepositoryCoverageBadgeService(private val coverageService: CoverageService, private val previousTestRunService: PreviousTestRunService) {
    private val svgCoverageBadgeCreator = SvgCoverageBadgeCreator()

    suspend fun createCoverageBadge(fullRepoName: String, projectName: String?): String? {

        val mostRecentMainBranchRunWithCoverage = previousTestRunService.findMostRecentMainBranchRunWithCoverage(fullRepoName, projectName)

        val coveredPercentage = mostRecentMainBranchRunWithCoverage?.let { publicId ->
            coverageService.getCoverage(publicId)
        }?.overallStats?.lineStat?.coveredPercentage

        return coveredPercentage?.let { svgCoverageBadgeCreator.createBadge(it) }
    }
}
