package projektor.plugin.testkit.notification

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import projektor.plugin.SpecWriter
import projektor.plugin.notification.link.LinkModel
import projektor.plugin.testkit.SingleProjectSpec

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class LinkFileSingleProjectSpec extends SingleProjectSpec {
    ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)

    def "when link file enabled and in CI should write link file"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                autoPublishOnFailureOnly = false
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithPassingTest(projectRootDir, "SampleSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = runSuccessfulBuildWithEnvironment(["CI": "true"], 'test')

        then:
        result.task(":test").outcome == SUCCESS

        and:
        File linkFile = new File(projectRootDir.root, "projektor_report.json")
        linkFile.exists()

        LinkModel linkModel = objectMapper.readValue(linkFile, LinkModel)
        linkModel.reportUrl == "${serverUrl}/tests/ABC123"
        linkModel.id == "ABC123"
    }

    def "when link file enabled but not in CI should not write link file"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                autoPublishOnFailureOnly = false
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithPassingTest(projectRootDir, "SampleSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = runSuccessfulBuildWithEnvironment(["CI": "false"], 'test')

        then:
        result.task(":test").outcome == SUCCESS

        and:
        File linkFile = new File(projectRootDir.root, "projektor_report.json")
        !linkFile.exists()
    }
}
