package projektor.notification.github

import org.kohsuke.github.*

class GitHubCommentClient(
    private val clientConfig: GitHubClientConfig,
    private val jwtProvider: JwtProvider
) {

    fun addComment(orgName: String, repoName: String, issueId: Int, commentText: String?) {
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
        val ghIssue = repository.getIssue(issueId)
        ghIssue.comment(commentText)
    }
}
