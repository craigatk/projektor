package projektor.compare

import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import org.koin.core.inject
import projektor.DatabaseRepositoryTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.example.coverage.JacocoXmlLoader
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class PreviousTestRunServiceTest : DatabaseRepositoryTestCase() {
    @Test
    fun `should exclude test run that is newer than specified test run`() {
        val previousTestRunService: PreviousTestRunService by inject()

        val previousPublicId = randomPublicId()
        val previousWithDifferentProjectName = randomPublicId()
        val thisPublicId = randomPublicId()
        val newerPublicId = randomPublicId()

        val repoName = "${RandomStringUtils.randomAlphabetic(8)}/${RandomStringUtils.randomAlphabetic(8)}"

        val previousTestRun = testRunDBGenerator.createSimpleTestRun(previousPublicId)
        testRunDBGenerator.addGitMetadata(previousTestRun, repoName, true, "main", null)
        runBlocking { coverageService.saveReport(JacocoXmlLoader().serverApp(), previousPublicId) }

        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = previousWithDifferentProjectName,
                coverageText = JacocoXmlLoader().serverAppReduced(),
                repoName = repoName,
                branchName = "main",
                projectName = "other-project"
        )

        val thisTestRun = testRunDBGenerator.createSimpleTestRun(thisPublicId)
        testRunDBGenerator.addGitMetadata(thisTestRun, repoName, true, "main", null)
        runBlocking { coverageService.saveReport(JacocoXmlLoader().serverApp(), thisPublicId) }

        val newerTestRun = testRunDBGenerator.createSimpleTestRun(newerPublicId)
        testRunDBGenerator.addGitMetadata(newerTestRun, repoName, true, "main", null)
        runBlocking { coverageService.saveReport(JacocoXmlLoader().serverApp(), newerPublicId) }

        val returnedId = runBlocking { previousTestRunService.findPreviousMainBranchRunWithCoverage(thisPublicId) }
        expectThat(returnedId).isEqualTo(previousPublicId)

        val previousIdFromOldest = runBlocking { previousTestRunService.findPreviousMainBranchRunWithCoverage(previousPublicId) }
        expectThat(previousIdFromOldest).isNull()
    }

    @Test
    fun `should find previous test run with the same project name`() {
        val previousTestRunService: PreviousTestRunService by inject()

        val previousPublicId = randomPublicId()
        val previousWithDifferentProjectName = randomPublicId()
        val thisPublicId = randomPublicId()

        val repoName = "${RandomStringUtils.randomAlphabetic(8)}/${RandomStringUtils.randomAlphabetic(8)}"

        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = previousPublicId,
                coverageText = JacocoXmlLoader().serverAppReduced(),
                repoName = repoName,
                branchName = "main",
                projectName = "project"
        )

        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = previousWithDifferentProjectName,
                coverageText = JacocoXmlLoader().serverAppReduced(),
                repoName = repoName,
                branchName = "main",
                projectName = "other-project"
        )

        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = thisPublicId,
                coverageText = JacocoXmlLoader().serverAppReduced(),
                repoName = repoName,
                branchName = "main",
                projectName = "project"
        )

        val returnedId = runBlocking { previousTestRunService.findPreviousMainBranchRunWithCoverage(thisPublicId) }
        expectThat(returnedId).isEqualTo(previousPublicId)
    }
}
