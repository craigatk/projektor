package projektor.badge

import projektor.compare.PreviousTestRunService
import projektor.notification.badge.SvgTestRunBadgeCreator
import projektor.server.api.PublicId
import projektor.server.api.repository.BranchSearch
import projektor.server.api.repository.BranchType
import projektor.testrun.TestRunService

class TestRunBadgeService(
    private val previousTestRunService: PreviousTestRunService,
    private val testRunBadgeCreator: SvgTestRunBadgeCreator,
    private val testRunService: TestRunService,
) {
    suspend fun createTestsBadge(publicId: PublicId): String? {
        val testRun = testRunService.fetchTestRun(publicId)

        return if (testRun != null) {
            testRunBadgeCreator.createBadge(testRun.summary.passed)
        } else {
            null
        }
    }

    suspend fun createTestsBadge(
        repoName: String,
        projectName: String?,
    ): String? {
        val previousTestRun =
            previousTestRunService.findMostRecentRun(
                repoName,
                projectName,
                BranchSearch(branchType = BranchType.MAINLINE),
            )

        return if (previousTestRun != null) {
            testRunBadgeCreator.createBadge(previousTestRun.passed)
        } else {
            null
        }
    }
}
