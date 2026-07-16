package projektor.pullrequest

import projektor.repository.testrun.RepositoryTestRunRepository
import projektor.server.api.pullrequest.PullRequestFailureContext
import projektor.server.api.pullrequest.TestCaseFailureContext
import projektor.testcase.TestCaseService

class PullRequestFailureContextService(
    private val repositoryTestRunRepository: RepositoryTestRunRepository,
    private val testCaseService: TestCaseService,
) {
    suspend fun fetchFailureContext(
        orgName: String,
        repoName: String,
        pullRequestNumber: Int,
    ): PullRequestFailureContext? {
        // GIT_METADATA.REPO_NAME stores the org-qualified slug (e.g. "org/repo"), matching how every
        // other repo-scoped query in this app builds it (see ApiRoutes.kt's `fullRepoName`).
        val fullRepoName = "$orgName/$repoName"

        val publicId =
            repositoryTestRunRepository.fetchMostRecentTestRunPublicId(fullRepoName, pullRequestNumber)
                ?: return null

        val failedTestCases = testCaseService.fetchFailedTestCases(publicId)

        val testCaseContexts =
            failedTestCases.mapNotNull { testCase ->
                testCaseService.buildTestCaseDebugContext(publicId, testCase.testSuiteIdx, testCase.idx)
                    ?.let { debugContext -> TestCaseFailureContext(testCase.fullName, debugContext.markdown) }
            }

        return PullRequestFailureContext(publicId.id, testCaseContexts)
    }
}
