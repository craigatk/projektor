package projektor.compare

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.test.inject
import projektor.DatabaseRepositoryTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.repository.BranchSearch
import projektor.server.api.repository.BranchType
import projektor.server.example.coverage.JacocoXmlLoader
import projektor.util.randomOrgAndRepo
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isTrue
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class PreviousTestRunServiceTest : DatabaseRepositoryTestCase() {
    @Test
    fun `should exclude test run that is newer than specified test run`() {
        val previousTestRunService by inject<PreviousTestRunService>()

        val previousPublicId = randomPublicId()
        val previousWithDifferentProjectName = randomPublicId()
        val thisPublicId = randomPublicId()
        val newerPublicId = randomPublicId()

        val repoName = randomOrgAndRepo()

        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
            previousPublicId,
            JacocoXmlLoader().serverApp(),
            repoName,
        )

        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
            publicId = previousWithDifferentProjectName,
            coverageText = JacocoXmlLoader().serverAppReduced(),
            repoName = repoName,
            branchName = "main",
            projectName = "other-project",
        )

        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
            thisPublicId,
            JacocoXmlLoader().serverApp(),
            repoName,
        )

        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
            newerPublicId,
            JacocoXmlLoader().serverApp(),
            repoName,
        )

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

        val repoName = randomOrgAndRepo()

        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
            publicId = previousPublicId,
            coverageText = JacocoXmlLoader().serverAppReduced(),
            repoName = repoName,
            branchName = "main",
            projectName = "project",
        )

        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
            publicId = previousWithDifferentProjectName,
            coverageText = JacocoXmlLoader().serverAppReduced(),
            repoName = repoName,
            branchName = "main",
            projectName = "other-project",
        )

        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
            publicId = thisPublicId,
            coverageText = JacocoXmlLoader().serverAppReduced(),
            repoName = repoName,
            branchName = "main",
            projectName = "project",
        )

        val returnedId = runBlocking { previousTestRunService.findPreviousMainBranchRunWithCoverage(thisPublicId) }
        expectThat(returnedId).isEqualTo(previousPublicId)
    }

    @Test
    fun `should find most recent main branch run that has coverage data`() {
        val previousTestRunService: PreviousTestRunService by inject()

        val olderPublicId = randomPublicId()
        val oldPublicId = randomPublicId()
        val newestMainlineWithCoveragePublicId = randomPublicId()
        val newerFeatureBranchWithCoveragePublicId = randomPublicId()

        val repoName = randomOrgAndRepo()

        listOf(olderPublicId, oldPublicId, newestMainlineWithCoveragePublicId).forEach { publicId ->
            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId,
                JacocoXmlLoader().serverApp(),
                repoName,
                branchName = "main",
            )
        }

        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
            newerFeatureBranchWithCoveragePublicId,
            JacocoXmlLoader().serverApp(),
            repoName,
            branchName = "feature/dev",
        )

        val newerWithoutCoveragePublicId = randomPublicId()
        testRunDBGenerator.createSimpleTestRunInRepo(
            newerWithoutCoveragePublicId,
            repoName,
            true,
            null,
        )

        val mostRecentTestRun =
            runBlocking {
                previousTestRunService.findMostRecentRunWithCoverage(
                    repoName,
                    null,
                    BranchSearch(branchType = BranchType.MAINLINE),
                )
            }

        expectThat(mostRecentTestRun).isNotNull().and {
            get { publicId }.isEqualTo(newestMainlineWithCoveragePublicId)
        }
    }

    @Test
    fun `should find most recent run in all branches that has coverage data`() {
        val previousTestRunService: PreviousTestRunService by inject()

        val olderPublicId = randomPublicId()
        val oldPublicId = randomPublicId()
        val newestMainlineWithCoveragePublicId = randomPublicId()
        val newerFeatureBranchWithCoveragePublicId = randomPublicId()

        val repoName = randomOrgAndRepo()

        listOf(olderPublicId, oldPublicId, newestMainlineWithCoveragePublicId).forEach { publicId ->
            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId,
                JacocoXmlLoader().serverApp(),
                repoName,
                branchName = "main",
            )
        }

        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
            newerFeatureBranchWithCoveragePublicId,
            JacocoXmlLoader().serverApp(),
            repoName,
            branchName = "feature/dev",
        )

        val newerWithoutCoveragePublicId = randomPublicId()
        testRunDBGenerator.createSimpleTestRunInRepo(
            newerWithoutCoveragePublicId,
            repoName,
            true,
            null,
        )

        val mostRecentTestRun =
            runBlocking { previousTestRunService.findMostRecentRunWithCoverage(repoName, null, BranchSearch(branchType = BranchType.ALL)) }

        expectThat(mostRecentTestRun).isNotNull().and {
            get { publicId }.isEqualTo(newerFeatureBranchWithCoveragePublicId)
        }
    }

    @Test
    fun `should find most recent main branch run in project that has coverage data`() {
        val previousTestRunService: PreviousTestRunService by inject()

        val previousPublicId = randomPublicId()
        val newPublicId = randomPublicId()
        val newerWithDifferentProjectName = randomPublicId()

        val repoName = randomOrgAndRepo()
        val projectName = "project"

        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
            publicId = previousPublicId,
            coverageText = JacocoXmlLoader().serverAppReduced(),
            repoName = repoName,
            branchName = "main",
            projectName = projectName,
        )

        val newTestRun =
            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = newPublicId,
                coverageText = JacocoXmlLoader().serverAppReduced(),
                repoName = repoName,
                branchName = "main",
                projectName = projectName,
            )

        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
            publicId = newerWithDifferentProjectName,
            coverageText = JacocoXmlLoader().serverAppReduced(),
            repoName = repoName,
            branchName = "main",
            projectName = "other-project",
        )

        val recentTestRun =
            runBlocking {
                previousTestRunService.findMostRecentRunWithCoverage(
                    repoName,
                    projectName,
                    BranchSearch(branchType = BranchType.MAINLINE),
                )
            }
        expectThat(recentTestRun).isNotNull().and {
            get { publicId }.isEqualTo(newPublicId)
            get {
                createdTimestamp.truncatedTo(ChronoUnit.MILLIS)
            }.isEqualTo(newTestRun.createdTimestamp.toInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS))
            get { branch }.isEqualTo("main")
        }
    }

    @Test
    fun shouldFindMostRecentTestRunWithoutProject() {
        val previousTestRunService: PreviousTestRunService by inject()

        val previousPublicId = randomPublicId()
        val newPublicId = randomPublicId()
        val newerWithDifferentProjectName = randomPublicId()

        val repoName = randomOrgAndRepo()

        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
            publicId = previousPublicId,
            coverageText = JacocoXmlLoader().serverAppReduced(),
            repoName = repoName,
            branchName = "main",
            projectName = null,
        )

        val newTestRun =
            testRunDBGenerator.createSimpleTestRunInRepo(
                publicId = newPublicId,
                repoName = repoName,
                ci = true,
                projectName = null,
            )

        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
            publicId = newerWithDifferentProjectName,
            coverageText = JacocoXmlLoader().serverAppReduced(),
            repoName = repoName,
            branchName = "main",
            projectName = "other-project",
        )

        val recentTestRun =
            runBlocking { previousTestRunService.findMostRecentRun(repoName, null, BranchSearch(branchType = BranchType.MAINLINE)) }
        expectThat(recentTestRun).isNotNull().and {
            get { publicId }.isEqualTo(newPublicId)
            get {
                createdTimestamp.truncatedTo(ChronoUnit.MILLIS)
            }.isEqualTo(newTestRun.createdTimestamp.toInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS))
            get { branch }.isEqualTo("main")
        }
    }

    @Test
    fun shouldFindMostRecentTestRunWithProject() {
        val previousTestRunService: PreviousTestRunService by inject()

        val previousPublicId = randomPublicId()
        val newPublicId = randomPublicId()
        val newerWithDifferentProjectName = randomPublicId()

        val repoName = randomOrgAndRepo()
        val projectName = "project"

        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
            publicId = previousPublicId,
            coverageText = JacocoXmlLoader().serverAppReduced(),
            repoName = repoName,
            branchName = "main",
            projectName = projectName,
        )

        val newTestRun =
            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = newPublicId,
                coverageText = JacocoXmlLoader().serverAppReduced(),
                repoName = repoName,
                branchName = "main",
                projectName = projectName,
            )

        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
            publicId = newerWithDifferentProjectName,
            coverageText = JacocoXmlLoader().serverAppReduced(),
            repoName = repoName,
            branchName = "main",
            projectName = "other-project",
        )

        val recentTestRun =
            runBlocking { previousTestRunService.findMostRecentRun(repoName, projectName, BranchSearch(branchType = BranchType.MAINLINE)) }
        expectThat(recentTestRun).isNotNull().and {
            get { publicId }.isEqualTo(newPublicId)
            get {
                createdTimestamp.truncatedTo(ChronoUnit.MILLIS)
            }.isEqualTo(newTestRun.createdTimestamp.toInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS))
            get { branch }.isEqualTo("main")
            get { passed }.isTrue()
        }
    }
}
