package projektor.plugin.testkit.notification

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import projektor.plugin.SettingsFileWriter
import projektor.plugin.SpecWriter
import projektor.plugin.notification.slack.message.SlackAttachment
import projektor.plugin.notification.slack.message.SlackAttachmentsMessage
import projektor.plugin.testkit.SingleProjectSpec

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static projektor.plugin.PluginOutput.verifyOutputContainsReportLink

class SlackMessageNotificationSingleProjectSpec extends SingleProjectSpec {
    ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)

    def "when Slack message enabled should write it"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                writeSlackMessageFile = true
            }
        """.stripIndent()

        File settingsGradleFile = SettingsFileWriter.createSettingsFile(projectRootDir, "slack-project")
        println "settings.gradle contents: ${settingsGradleFile.text}"

        SpecWriter.createTestDirectoryWithFailingTest(projectRootDir, "SampleFailingSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = runFailedBuild('test')

        then:
        result.task(":test").outcome == FAILED

        and:
        File slackMessageFile = new File(projectRootDir.root, "projektor_failure_message.json")
        slackMessageFile.exists()

        println slackMessageFile.text

        SlackAttachmentsMessage slackMessage = objectMapper.readValue(slackMessageFile, SlackAttachmentsMessage)
        slackMessage.attachments.size() == 1

        SlackAttachment slackAttachment = slackMessage.attachments[0]
        slackAttachment.pretext == "Tests failed in project slack-project"
        slackAttachment.titleLink == "${serverUrl}/tests/ABC123"

        and:
        verifyOutputContainsReportLink(result.output, serverUrl, resultsId)
    }

    def "when Slack message not enabled should not write it"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        File settingsGradleFile = SettingsFileWriter.createSettingsFile(projectRootDir, "slack-project")
        println "settings.gradle contents: ${settingsGradleFile.text}"

        SpecWriter.createTestDirectoryWithFailingTest(projectRootDir, "SampleFailingSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = runFailedBuild('test')

        then:
        result.task(":test").outcome == FAILED

        and:
        File slackMessageFile = new File(projectRootDir.root, "projektor_failure_message.json")
        !slackMessageFile.exists()

        and:
        verifyOutputContainsReportLink(result.output, serverUrl, resultsId)
    }
}
