package projektor.notification.github

import org.kohsuke.github.*
import projektor.notification.github.auth.JwtProvider

class GitHubCommentClient(
    private val clientConfig: GitHubClientConfig,
    private val jwtProvider: JwtProvider
) {

    fun addComment(repository: GHRepository, issueId: Int, commentText: String) {
        val ghIssue = repository.getIssue(issueId)
        ghIssue.comment(commentText)
    }

    fun findCommentWithText(repository: GHRepository, issueId: Int, commentText: String): GHIssueComment? {
        val ghIssue = repository.getIssue(issueId)

        val comments = ghIssue.listComments().toList()

        return comments.find { it.body.contains(commentText) }
    }

    fun updateComment(comment: GHIssueComment, newText: String) {
        comment.update(newText)
    }

    fun findOpenPullRequestsForBranch(repository: GHRepository, branchName: String): Int? {
        val pullRequestsResults = repository
            .queryPullRequests()
            .state(GHIssueState.OPEN)
            .list()
            .toList()

        val pullRequestForBranch = pullRequestsResults
            .firstOrNull { it.head.ref.endsWith(branchName) }

        return pullRequestForBranch?.number
    }

    fun getRepository(orgName: String, repoName: String): GHRepository {
        val jwtToken = jwtProvider.createJWT()

        val gitHub = GitHubBuilder()
            .withJwtToken(jwtToken)
            .withEndpoint(this.clientConfig.gitHubApiUrl)
            .build()
        val gitHubApp = gitHub.app
        val appInstallation = gitHubApp.getInstallationByRepository(orgName, repoName)
        val appInstallationToken = appInstallation.createToken().create()
        val githubAuthAsInst = GitHubBuilder()
            .withAppInstallationToken(appInstallationToken.token)
            .withEndpoint(this.clientConfig.gitHubApiUrl)
            .build()
        val repository = githubAuthAsInst.getRepository("$orgName/$repoName")

        return repository
    }
}
