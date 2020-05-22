package projektor.plugin.notification

class NotificationConfig {
    final boolean writeSlackMessageFile
    final String slackMessageFileName

    NotificationConfig(
            boolean writeSlackMessageFile,
            String slackMessageFileName
    ) {
        this.writeSlackMessageFile = writeSlackMessageFile
        this.slackMessageFileName = slackMessageFileName
    }
}
