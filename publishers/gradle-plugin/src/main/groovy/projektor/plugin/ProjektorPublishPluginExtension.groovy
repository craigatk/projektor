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

    boolean codeCoveragePublish = false
}
