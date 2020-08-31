package projektor.plugin

import org.gradle.api.file.FileTree

class ProjektorPublishPluginExtension {
    boolean autoPublish = true
    boolean autoPublishOnFailureOnly = true
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

    boolean codeCoveragePublish = true
    boolean autoPublishWhenCoverageInCI = true

    boolean gitInfoEnabled = true
    List<String> gitMainBranchNames = ["main", "master"]
    List<String> gitRepoEnvironmentVariables = ["VELA_REPO_FULL_NAME", "GITHUB_REPOSITORY"]
    List<String> gitRefEnvironmentVariables = ["VELA_BUILD_REF", "GITHUB_REF"]
}
