package projektor.notification.github;

public class GitHubClientConfig {
    public final String githubAppId;
    public final String pemContents;
    public final long ttlMillis;
    public final String gitHubApiUrl;

    public GitHubClientConfig(String githubAppId, String pemContents, long ttlMillis, String gitHubApiUrl) {
        this.githubAppId = githubAppId;
        this.pemContents = pemContents;
        this.ttlMillis = ttlMillis;
        this.gitHubApiUrl = gitHubApiUrl;
    }
}
