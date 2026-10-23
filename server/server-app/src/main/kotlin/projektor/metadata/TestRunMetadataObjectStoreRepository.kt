package projektor.metadata

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import projektor.persistence.objectstore.ObjectStoreKeys
import projektor.persistence.objectstore.TestRunDocument
import projektor.persistence.objectstore.TestRunDocumentStore
import projektor.persistence.objectstore.TestRunIndex
import projektor.server.api.PublicId
import projektor.server.api.metadata.TestRunGitMetadata
import projektor.server.api.metadata.TestRunMetadata

class TestRunMetadataObjectStoreRepository(
    private val documentStore: TestRunDocumentStore,
    private val testRunIndex: TestRunIndex,
) : TestRunMetadataRepository {
    override suspend fun fetchGitMetadata(publicId: PublicId): TestRunGitMetadata? =
        documentStore.readTestRun(publicId)?.gitMetadata?.let { git ->
            TestRunGitMetadata(
                repoName = git.repoName,
                orgName = git.orgName,
                branchName = git.branchName,
                projectName = git.projectName,
                isMainBranch = git.isMainBranch,
                pullRequestNumber = git.pullRequestNumber,
                commitSha = git.commitSha,
            )
        }

    override suspend fun fetchResultsMetadata(publicId: PublicId): TestRunMetadata? =
        documentStore.readTestRun(publicId)?.resultsMetadata?.let { TestRunMetadata(it.ci) }

    override suspend fun updateGitMetadata(
        publicId: PublicId,
        pullRequestNumber: Int?,
        commitSha: String?,
    ) {
        withContext(Dispatchers.IO) {
            // Like the database repository, does nothing when the test run doesn't have Git metadata
            documentStore.store.update<TestRunDocument>(ObjectStoreKeys.testRun(publicId)) { current ->
                current?.takeUnless { it.deleted }?.gitMetadata?.let { git ->
                    current.copy(gitMetadata = git.copy(pullRequestNumber = pullRequestNumber, commitSha = commitSha))
                }
            }
        }

        testRunIndex.refreshEntry(publicId)
    }
}
