package projektor.repository.testrun

import projektor.persistence.objectstore.TestRunIndex
import projektor.persistence.objectstore.TestRunIndexEntry
import projektor.persistence.objectstore.mapParallel
import projektor.persistence.objectstore.toApi
import projektor.server.api.PublicId
import projektor.server.api.TestCase
import projektor.server.api.TestRunSummary
import projektor.server.api.repository.BranchType
import projektor.server.api.repository.RepositoryTestRunTimeline
import projektor.server.api.repository.RepositoryTestRunTimelineEntry
import projektor.testcase.TestCaseRepository

class RepositoryTestRunObjectStoreRepository(
    private val testRunIndex: TestRunIndex,
    private val testCaseRepository: TestCaseRepository,
) : RepositoryTestRunRepository {
    override suspend fun fetchRepositoryTestRunTimeline(
        repoName: String,
        projectName: String?,
    ): RepositoryTestRunTimeline? {
        val timelineEntries =
            ciRunsFromRepo(repoName, projectName).map { entry ->
                RepositoryTestRunTimelineEntry(
                    publicId = entry.publicId,
                    createdTimestamp = entry.createdTimestamp,
                    cumulativeDuration = entry.summary.cumulativeDuration,
                    wallClockDuration = entry.summary.wallClockDuration,
                    totalTestCount = entry.summary.totalTestCount,
                    passed = entry.summary.passed,
                )
            }

        return if (timelineEntries.isNotEmpty()) RepositoryTestRunTimeline(timelineEntries) else null
    }

    override suspend fun fetchRepositoryTestRunSummaries(
        repoName: String,
        projectName: String?,
        limit: Int,
    ): List<TestRunSummary> =
        testRunIndex.readNewestEntries(repoName, limit) { it.isCiRunOf(projectName) && it.isMainBranch }
            .take(limit)
            .map { it.summary.toApi(PublicId(it.publicId)) }

    override suspend fun fetchRepositoryFailingTestCases(
        repoName: String,
        projectName: String?,
        maxRuns: Int,
        branchType: BranchType,
    ): List<TestCase> {
        val matchingRuns =
            testRunIndex.readNewestEntries(
                repoName,
                maxRuns,
            ) { it.isCiRunOf(projectName) && it.matchesBranchType(branchType) }
        val oldestRecentRun = matchingRuns.take(maxRuns).lastOrNull() ?: return listOf()

        // Like the database query, includes every run created at or after the oldest of the most recent runs
        return matchingRuns
            .filter { it.createdTimestamp >= oldestRecentRun.createdTimestamp && it.summary.totalFailureCount > 0 }
            .mapParallel { entry -> testCaseRepository.fetchFailedTestCases(PublicId(entry.publicId)) }
            .flatten()
    }

    override suspend fun fetchRecentTestRunPublicIds(
        repoName: String,
        projectName: String?,
        maxRuns: Int,
    ): List<PublicId> =
        testRunIndex.readNewestEntries(repoName, maxRuns) { it.isCiRunOf(projectName) }
            .take(maxRuns)
            .map { PublicId(it.publicId) }

    override suspend fun fetchMostRecentTestRunPublicId(
        repoName: String,
        pullRequestNumber: Int,
    ): PublicId? =
        testRunIndex.findNewestEntry(repoName) { it.pullRequestNumber == pullRequestNumber }
            ?.let { PublicId(it.publicId) }

    override suspend fun fetchMostRecentTestRunPublicIdWithCoverage(
        repoName: String,
        pullRequestNumber: Int,
    ): PublicId? =
        testRunIndex.findNewestEntry(repoName) { it.pullRequestNumber == pullRequestNumber && it.hasCoverageRun }
            ?.let { PublicId(it.publicId) }

    override suspend fun fetchTestRunCount(
        repoName: String,
        projectName: String?,
    ): Long = ciRunsFromRepo(repoName, projectName).size.toLong()

    private suspend fun ciRunsFromRepo(
        repoName: String,
        projectName: String?,
    ): List<TestRunIndexEntry> = testRunIndex.readRepositoryEntries(repoName).filter { it.isCiRunOf(projectName) }

    private fun TestRunIndexEntry.isCiRunOf(projectName: String?) = matchesProject(projectName) && isCI
}

fun TestRunIndexEntry.matchesBranchType(branchType: BranchType?) = branchType != BranchType.MAINLINE || isMainBranch
