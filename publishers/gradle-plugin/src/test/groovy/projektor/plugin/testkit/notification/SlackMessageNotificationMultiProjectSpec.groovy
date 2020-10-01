package projektor.plugin.testkit.notification

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import projektor.plugin.SpecWriter
import projektor.plugin.notification.slack.message.SlackAttachment
import projektor.plugin.notification.slack.message.SlackAttachmentsMessage
import projektor.plugin.testkit.MultiProjectSpec

import static projektor.plugin.PluginOutput.verifyOutputContainsReportLink

class SlackMessageNotificationMultiProjectSpec extends MultiProjectSpec {
    ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)

    def "when Slack message enabled should write it"() {
        given:
        rootBuildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                writeSlackMessageFile = true
                alwaysPublish = true
            }
        """.stripIndent()

        settingsFile << """rootProject.name = 'slack-multi-project'"""

        SpecWriter.writePassingSpecFiles(testDirectory1, ["Sample1Spec1", "Sample1Spec2"])
        SpecWriter.writePassingSpecFiles(testDirectory2, ["Sample2Spec1", "Sample2Spec2"])
        SpecWriter.writePassingSpecFiles(testDirectory3, ["Sample3Spec1", "Sample3Spec2"])

        String resultsId = "DEF456"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = runSuccessfulBuild('test')

        then:
        File slackMessageFile = new File(projectRootDir.root, "projektor_failure_message.json")
        slackMessageFile.exists()

        println slackMessageFile.text

        SlackAttachmentsMessage slackMessage = objectMapper.readValue(slackMessageFile, SlackAttachmentsMessage)
        slackMessage.attachments.size() == 1

        SlackAttachment slackAttachment = slackMessage.attachments[0]
        slackAttachment.pretext == "Tests failed in project slack-multi-project"
        slackAttachment.titleLink == "${serverUrl}/tests/DEF456"

        and:
        verifyOutputContainsReportLink(result.output, serverUrl, resultsId)
    }
}
