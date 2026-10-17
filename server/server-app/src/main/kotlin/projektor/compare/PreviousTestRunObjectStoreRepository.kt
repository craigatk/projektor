package projektor.compare

import projektor.persistence.objectstore.TestRunDocumentStore
import projektor.persistence.objectstore.TestRunIndex
import projektor.persistence.objectstore.TestRunIndexEntry
import projektor.persistence.objectstore.newestFirst
import projektor.repository.testrun.matchesBranchType
import projektor.server.api.PublicId
import projektor.server.api.repository.BranchSearch

class PreviousTestRunObjectStoreRepository(
    private val documentStore: TestRunDocumentStore,
    private val testRunIndex: TestRunIndex,
) : PreviousTestRunRepository {
    override suspend fun findPreviousMainBranchRunWithCoverage(publicId: PublicId): PublicId? =
        findPreviousMainBranchRun(publicId) { it.hasCoverageRun }?.let { PublicId(it.publicId) }

    override suspend fun findPreviousMainBranchRun(publicId: PublicId): RecentTestRun? =
        findPreviousMainBranchRun(publicId) {
            it.isCI
        }?.toRecentTestRun()

    override suspend fun findMostRecentRunWithCoverage(
        repoName: String,
        projectName: String?,
        branch: BranchSearch?,
    ): RecentTestRun? = findMostRecentRun(repoName, projectName, branch) { it.hasCoverageRun }

    override suspend fun findMostRecentRun(
        repoName: String,
        projectName: String?,
        branch: BranchSearch?,
    ): RecentTestRun? = findMostRecentRun(repoName, projectName, branch) { true }

    private suspend fun findPreviousMainBranchRun(
        publicId: PublicId,
        additionalCondition: (TestRunIndexEntry) -> Boolean,
    ): TestRunIndexEntry? {
        val currentTestRun = documentStore.readTestRun(publicId) ?: return null
        val currentGit = currentTestRun.gitMetadata ?: return null

        return testRunIndex.readRepositoryEntries(currentGit.repoName).newestFirst().firstOrNull { entry ->
            entry.isMainBranch &&
                entry.publicId != publicId.id &&
                entry.matchesProject(currentGit.projectName) &&
                entry.createdTimestamp < currentTestRun.summary.createdTimestamp &&
                additionalCondition(entry)
        }
    }

    private suspend fun findMostRecentRun(
        repoName: String,
        projectName: String?,
        branch: BranchSearch?,
        additionalCondition: (TestRunIndexEntry) -> Boolean,
    ): RecentTestRun? =
        testRunIndex.readRepositoryEntries(repoName).newestFirst().firstOrNull { entry ->
            entry.matchesBranchType(branch?.branchType) &&
                entry.matchesBranchName(branch?.branchName) &&
                entry.matchesProject(projectName) &&
                additionalCondition(entry)
        }?.toRecentTestRun()

    private fun TestRunIndexEntry.toRecentTestRun() =
        RecentTestRun(
            publicId = PublicId(publicId),
            createdTimestamp = createdTimestamp,
            passed = summary.passed,
            branch = branchName,
        )
}
