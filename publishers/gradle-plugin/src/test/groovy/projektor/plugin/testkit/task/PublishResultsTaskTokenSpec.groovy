package projektor.plugin.testkit.task

import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import projektor.plugin.SpecWriter
import projektor.plugin.client.ClientToken
import projektor.plugin.testkit.SingleProjectSpec

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static projektor.plugin.PluginOutput.verifyOutputContainsReportLink

class PublishResultsTaskTokenSpec extends SingleProjectSpec {

    def "should publish results with publish task including publish token"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                publishToken = 'token12345'
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithPassingTest(projectRootDir, "SampleSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def testResult = runSuccessfulLocalBuild('test')

        then:
        testResult.task(":test").outcome == SUCCESS

        and:
        resultsStubber.findResultsRequests().size() == 0

        when:
        def publishResults = runSuccessfulLocalBuild('publishResults')

        then:
        publishResults.task(":publishResults").outcome == SUCCESS

        and:
        verifyOutputContainsReportLink(publishResults.output, serverUrl, resultsId)

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1

        HttpHeader publishTokenInHeader = resultsRequests[0].header(ClientToken.PUBLISH_TOKEN_NAME)
        publishTokenInHeader.firstValue() == "token12345"
    }
}
