package projektor.repository.testrun

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import projektor.persistence.objectstore.TestRunIndex
import projektor.persistence.objectstore.TestRunIndexEntry
import projektor.persistence.objectstore.newestFirst
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
        ciRunsFromRepo(repoName, projectName)
            .filter { it.isMainBranch }
            .newestFirst()
            .take(limit)
            .map { it.summary.toApi(PublicId(it.publicId)) }

    override suspend fun fetchRepositoryFailingTestCases(
        repoName: String,
        projectName: String?,
        maxRuns: Int,
        branchType: BranchType,
    ): List<TestCase> =
        coroutineScope {
            val matchingRuns = ciRunsFromRepo(repoName, projectName).filter { it.matchesBranchType(branchType) }
            val oldestRecentRun = matchingRuns.newestFirst().take(maxRuns).lastOrNull() ?: return@coroutineScope listOf()

            // Like the database query, includes every run created at or after the oldest of the most recent runs
            matchingRuns
                .filter { it.createdTimestamp >= oldestRecentRun.createdTimestamp && it.summary.totalFailureCount > 0 }
                .newestFirst()
                .map { entry -> async(Dispatchers.IO) { testCaseRepository.fetchFailedTestCases(PublicId(entry.publicId)) } }
                .awaitAll()
                .flatten()
        }

    override suspend fun fetchRecentTestRunPublicIds(
        repoName: String,
        projectName: String?,
        maxRuns: Int,
    ): List<PublicId> =
        ciRunsFromRepo(repoName, projectName)
            .newestFirst()
            .take(maxRuns)
            .map { PublicId(it.publicId) }

    override suspend fun fetchMostRecentTestRunPublicId(
        repoName: String,
        pullRequestNumber: Int,
    ): PublicId? =
        testRunIndex.readRepositoryEntries(repoName)
            .newestFirst()
            .firstOrNull { it.pullRequestNumber == pullRequestNumber }
            ?.let { PublicId(it.publicId) }

    override suspend fun fetchMostRecentTestRunPublicIdWithCoverage(
        repoName: String,
        pullRequestNumber: Int,
    ): PublicId? =
        testRunIndex.readRepositoryEntries(repoName)
            .newestFirst()
            .firstOrNull { it.pullRequestNumber == pullRequestNumber && it.hasCoverageRun }
            ?.let { PublicId(it.publicId) }

    override suspend fun fetchTestRunCount(
        repoName: String,
        projectName: String?,
    ): Long = ciRunsFromRepo(repoName, projectName).size.toLong()

    private suspend fun ciRunsFromRepo(
        repoName: String,
        projectName: String?,
    ): List<TestRunIndexEntry> = testRunIndex.readRepositoryEntries(repoName).filter { it.matchesProject(projectName) && it.isCI }
}

fun TestRunIndexEntry.matchesBranchType(branchType: BranchType?) = branchType != BranchType.MAINLINE || isMainBranch
