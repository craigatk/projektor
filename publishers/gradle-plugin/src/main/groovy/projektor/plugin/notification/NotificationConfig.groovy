package projektor.plugin.notification

class NotificationConfig {
    final boolean writeSlackMessageFile
    final String slackMessageFileName
    final boolean writeLinkFile
    final String linkFileName

    NotificationConfig(
            boolean writeSlackMessageFile,
            String slackMessageFileName,
            boolean writeLinkFile,
            String linkFileName
    ) {
        this.writeSlackMessageFile = writeSlackMessageFile
        this.slackMessageFileName = slackMessageFileName
        this.writeLinkFile = writeLinkFile
        this.linkFileName = linkFileName
    }
}
