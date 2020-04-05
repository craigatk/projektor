package projektor.plugin.testkit

import com.github.tomakehurst.wiremock.verification.LoggedRequest
import projektor.plugin.SpecWriter

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static projektor.plugin.PluginOutput.verifyOutputContainsReportLink
import static projektor.plugin.PluginOutput.verifyOutputDoesNotContainReportLink

class PluginAutoPublishFailureOnlySpec extends SingleProjectSpec {

    def "when tests fail and publish-on-failure-only enabled should publish results to server"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                autoPublishOnFailureOnly = true
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithFailingTest(projectRootDir, "SampleFailingSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = runFailedBuild('test')

        then:
        result.task(":test").outcome == FAILED

        and:
        verifyOutputContainsReportLink(result.output, serverUrl, resultsId)

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1

        String resultsBody = resultsRequests[0].bodyAsString
        resultsBody.contains("SampleFailingSpec")
    }

    def "when tests pass and publish-on-failure-only enabled should not publish results to server"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                autoPublishOnFailureOnly = true
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithPassingTest(projectRootDir, "SampleSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = runSuccessfulBuild('test')

        then:
        result.task(":test").outcome == SUCCESS

        and:
        verifyOutputDoesNotContainReportLink(result.output)

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 0
    }

}
