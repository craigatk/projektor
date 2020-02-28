package projektor.plugin

import org.gradle.api.file.FileTree

class ProjektorPublishPluginExtension {
    boolean autoPublish = true
    boolean autoPublishOnFailureOnly = false
    String serverUrl
    boolean manualPublishEnabled = true
    String publishToken
    List<String> additionalResultsDirs = []
    List<FileTree> attachments = []
}
