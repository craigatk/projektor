package projektor.plugin

import org.gradle.api.file.FileTree

class ProjektorPublishPluginExtension {
    boolean alwaysPublishInCI = true
    boolean publishOnLocalFailure = true
    boolean alwaysPublish = false

    String serverUrl
    boolean publishTaskEnabled = true
    String publishToken
    List<String> additionalResultsDirs = []
    List<FileTree> attachments = []
    boolean compressionEnabled = true

    int publishRetryMaxAttempts = 3
    long publishRetryInterval = 100
    long publishTimeout = 10_000

    boolean writeSlackMessageFile = false
    String slackMessageFileName = "projektor_failure_message.json"

    boolean writeLinkFile = true
    String linkFileName = "projektor_report.json"

    boolean codeCoveragePublish = true

    boolean gitInfoEnabled = true
    List<String> gitMainBranchNames = ["main", "master"]
    List<String> gitRepoEnvironmentVariables = ["VELA_REPO_FULL_NAME", "DRONE_REPO", "GITHUB_REPOSITORY"]
    List<String> gitRefEnvironmentVariables = ["VELA_BUILD_REF", "GITHUB_REF"]
    List<String> gitBranchEnvironmentVariables = ["VELA_PULL_REQUEST_SOURCE", "DRONE_REPO_BRANCH"]
    List<String> gitCommitShaEnvironmentVariables = ["VELA_BUILD_COMMIT", "GITHUB_SHA"]
    List<String> gitPullRequestNumberEnvironmentVariables = ["VELA_BUILD_PULL_REQUEST"]

    List<String> ciEnvironmentVariables = ["CI", "VELA", "DRONE"]
}
