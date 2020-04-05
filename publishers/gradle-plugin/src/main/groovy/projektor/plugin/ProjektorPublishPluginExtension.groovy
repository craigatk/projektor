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
}
