package projektor.plugin.testkit.task

import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.gradle.testkit.runner.GradleRunner
import projektor.plugin.testkit.SingleProjectSpec

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static projektor.plugin.results.ProjektorResultsClient.getPUBLISH_TOKEN_NAME

class PublishResultsTaskTokenSpec extends SingleProjectSpec {

    def "should publish results with publish task including publish token"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                autoPublish = false
                publishToken = 'token12345'
            }
        """.stripIndent()

        File testDirectory = specWriter.createTestDirectory(projectRootDir)
        specWriter.writeSpecFile(testDirectory, "SampleSpec")

        String resultsId = "ABC123"
        wireMockStubber.stubResultsPostSuccess(resultsId)

        when:
        def testResult = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test')
                .withPluginClasspath()
                .build()

        then:
        testResult.task(":test").outcome == SUCCESS

        and:
        wireMockStubber.findResultsRequests().size() == 0

        when:
        def publishResults = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('publishResults')
                .withPluginClasspath()
                .build()

        then:
        publishResults.task(":publishResults").outcome == SUCCESS

        and:
        publishResults.output.contains("View Projektor report at: ${serverUrl}/tests/${resultsId}")

        and:
        List<LoggedRequest> resultsRequests = wireMockStubber.findResultsRequests()
        resultsRequests.size() == 1

        HttpHeader publishTokenInHeader = resultsRequests[0].header(PUBLISH_TOKEN_NAME)
        publishTokenInHeader.firstValue() == "token12345"
    }

}
