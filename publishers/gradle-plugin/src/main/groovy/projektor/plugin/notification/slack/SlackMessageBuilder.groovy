package projektor.plugin.notification.slack

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import projektor.plugin.notification.slack.message.SlackAttachment
import projektor.plugin.notification.slack.message.SlackAttachmentsMessage

@Builder(builderStrategy = SimpleStrategy)
class SlackMessageBuilder {
    String projectName
    String projektorUrl

    SlackAttachmentsMessage buildAttachmentsMessage() {
        SlackAttachment slackAttachment = new SlackAttachment(
                fallback: "Projektor test report",
                color: "#FF0000",
                pretext: "Tests failed in project ${projectName}",
                title: "Projektor test report",
                titleLink: projektorUrl,
                text: "See the Projektor test report for details on the failing tests",
                footer: "Projektor",
                ts: System.currentTimeMillis()
        )

        return new SlackAttachmentsMessage(attachments: [slackAttachment])
    }
}
