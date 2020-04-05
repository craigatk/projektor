package projektor.plugin.testkit.task

import com.github.tomakehurst.wiremock.verification.LoggedRequest
import projektor.plugin.SpecWriter
import projektor.plugin.testkit.SingleProjectSpec

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static projektor.plugin.PluginOutput.verifyOutputContainsReportLink

class PublishResultsTaskSingleProjectSpec extends SingleProjectSpec {

    def "should publish results with publish task"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                autoPublish = false
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithPassingTest(projectRootDir, "SampleSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def testResult = runSuccessfulBuild('test')

        then:
        testResult.task(":test").outcome == SUCCESS

        and:
        resultsStubber.findResultsRequests().size() == 0

        when:
        def publishResults = runSuccessfulBuild('publishResults')

        then:
        publishResults.task(":publishResults").outcome == SUCCESS

        and:
        verifyOutputContainsReportLink(publishResults.output, serverUrl, resultsId)

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1

        and:
        String requestBody = resultsRequests[0].bodyAsString

        requestBody.contains('SampleSpec')
        requestBody.contains('sample test')
    }

}
