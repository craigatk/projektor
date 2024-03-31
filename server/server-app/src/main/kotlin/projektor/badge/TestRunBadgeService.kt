package projektor.badge

import projektor.compare.PreviousTestRunService
import projektor.notification.badge.SvgTestRunBadgeCreator
import projektor.server.api.repository.BranchSearch
import projektor.server.api.repository.BranchType

class TestRunBadgeService(
    private val previousTestRunService: PreviousTestRunService,
    private val testRunBadgeCreator: SvgTestRunBadgeCreator,
) {
    suspend fun createTestsBadge(repoName: String, projectName: String?): String? {
        val previousTestRun = previousTestRunService.findMostRecentRun(
            repoName,
            projectName,
            BranchSearch(branchType = BranchType.MAINLINE)
        )

        return if (previousTestRun != null) {
            testRunBadgeCreator.createBadge(previousTestRun.passed)
        } else {
            null
        }
    }
}
