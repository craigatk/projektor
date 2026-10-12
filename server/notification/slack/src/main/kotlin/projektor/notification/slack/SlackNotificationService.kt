package projektor.notification.slack

import projektor.notification.slack.message.SlackMessageCreator
import projektor.notification.slack.message.SlackNotificationData

class SlackNotificationService(private val slackClient: SlackClient) {
    fun sendNotification(
        channel: String,
        notificationData: SlackNotificationData,
    ) = slackClient.postMessage(SlackMessageCreator.createMessage(channel, notificationData))

    fun sendChannelConfiguredMessage(
        channel: String,
        repoName: String,
        projectName: String?,
    ) = slackClient.postMessage(SlackMessageCreator.createChannelConfiguredMessage(channel, repoName, projectName))
}
