package projektor.compare

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.test.inject
import projektor.DatabaseRepositoryTestCase
import projektor.incomingresults.randomPublicId
import projektor.util.randomOrgAndRepo
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isNull

class PreviousMainBranchRunServiceTest : DatabaseRepositoryTestCase() {
    @Test
    fun `should find most recent CI mainline run in same repo and project before the specified run`() {
        val previousTestRunService by inject<PreviousTestRunService>()

        val repoName = randomOrgAndRepo()

        val olderPublicId = randomPublicId()
        val previousPublicId = randomPublicId()
        val previousNotCIPublicId = randomPublicId()
        val previousFeatureBranchPublicId = randomPublicId()
        val previousOtherProjectPublicId = randomPublicId()
        val thisPublicId = randomPublicId()
        val newerPublicId = randomPublicId()

        testRunDBGenerator.createSimpleTestRunInRepo(olderPublicId, repoName, true, null)
        testRunDBGenerator.createSimpleFailingTestRunInRepo(previousPublicId, repoName, true, null)
        testRunDBGenerator.createSimpleTestRunInRepo(previousNotCIPublicId, repoName, false, null)
        testRunDBGenerator.createTestRunInRepo(previousFeatureBranchPublicId, listOf(), repoName, true, null, "feature")
        testRunDBGenerator.createSimpleTestRunInRepo(previousOtherProjectPublicId, repoName, true, "other-project")
        testRunDBGenerator.createSimpleTestRunInRepo(thisPublicId, repoName, true, null)
        testRunDBGenerator.createSimpleTestRunInRepo(newerPublicId, repoName, true, null)

        val previousRun = runBlocking { previousTestRunService.findPreviousMainBranchRun(thisPublicId) }

        expectThat(previousRun).isNotNull().and {
            get { publicId }.isEqualTo(previousPublicId)
            get { passed }.isFalse()
        }
    }

    @Test
    fun `should not find previous mainline run when there is none`() {
        val previousTestRunService by inject<PreviousTestRunService>()

        val repoName = randomOrgAndRepo()
        val thisPublicId = randomPublicId()

        testRunDBGenerator.createSimpleTestRunInRepo(thisPublicId, repoName, true, null)

        val previousRun = runBlocking { previousTestRunService.findPreviousMainBranchRun(thisPublicId) }

        expectThat(previousRun).isNull()
    }
}
