package projektor.plugin.notification.slack

import com.fasterxml.jackson.databind.ObjectMapper
import projektor.plugin.notification.NotificationConfig
import projektor.plugin.notification.slack.message.SlackAttachmentsMessage

class SlackMessageWriter {
    final ObjectMapper objectMapper = new ObjectMapper()

    File writeSlackMessage(
            SlackAttachmentsMessage slackMessage,
            NotificationConfig notificationConfig,
            File destinationDirectory
    ) {
        String fileText = objectMapper.writeValueAsString(slackMessage)

        File slackMessageFile = new File(destinationDirectory, notificationConfig.slackMessageFileName)
        slackMessageFile.text = fileText

        return slackMessageFile
    }
}
