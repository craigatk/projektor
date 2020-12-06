package projektor.notification.github;

import org.kohsuke.github.*;

public class GitHubCommentClient {
    private final GitHubClientConfig clientConfig;
    private final JwtProvider jwtProvider;

    public GitHubCommentClient(GitHubClientConfig clientConfig, JwtProvider jwtProvider) {
        this.clientConfig = clientConfig;
        this.jwtProvider = jwtProvider;
    }

    public void addComment(String orgName, String repoName, int issueId, String commentText) throws Exception {
        String jwtToken = this.jwtProvider.createJWT(
                this.clientConfig.githubAppId,
                this.clientConfig.pemContents,
                this.clientConfig.ttlMillis
        );
        GitHub gitHub = new GitHubBuilder()
                .withJwtToken(jwtToken)
                .withEndpoint(this.clientConfig.gitHubApiUrl)
                .build();

        GHApp gitHubApp = gitHub.getApp();
        GHAppInstallation appInstallation = gitHubApp.getInstallationByRepository(orgName, repoName);
        GHAppInstallationToken appInstallationToken = appInstallation.createToken().create();

        GitHub githubAuthAsInst = new GitHubBuilder()
                .withAppInstallationToken(appInstallationToken.getToken())
                .withEndpoint(this.clientConfig.gitHubApiUrl)
                .build();

        GHRepository repository = githubAuthAsInst.getRepository(orgName + "/" + repoName);

        GHIssue ghIssue = repository.getIssue(issueId);
        ghIssue.comment(commentText);
    }
}
