package projektor.plugin.testkit

import com.github.tomakehurst.wiremock.verification.LoggedRequest
import projektor.plugin.SpecWriter

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static projektor.plugin.PluginOutput.verifyOutputContainsReportLink
import static projektor.plugin.PluginOutput.verifyOutputDoesNotContainReportLink

class PluginAutoPublishSingleProjectSpec extends SingleProjectSpec {

    def "should publish results from test task to server"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithFailingTest(projectRootDir, "SampleSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = runFailedBuild('test')

        then:
        !result.output.contains("Projektor plugin enabled but no server specified")
        verifyOutputContainsReportLink(result.output, serverUrl, resultsId)

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1
    }

    def "when Projektor plugin is enabled but no server URL set should not send results to server"() {
        given:
        buildFile << """
            projektor {
                serverUrl = null
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithPassingTest(projectRootDir, "SampleSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = runSuccessfulBuildInCI('test')

        then:
        result.task(":test").outcome == SUCCESS

        and:
        result.output.contains("Projektor plugin enabled but no server specified")
        verifyOutputDoesNotContainReportLink(result.output)

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 0
    }
}
