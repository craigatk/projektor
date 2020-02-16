package projektor.plugin.testkit

import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.gradle.testkit.runner.GradleRunner

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static projektor.plugin.results.ProjektorResultsClient.PUBLISH_TOKEN_NAME

class PublishTokenPluginSpec extends SingleProjectSpec {
    def "should include publish token as header in publish request"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                publishToken = 'publish12345'
            }
        """.stripIndent()

        File testDirectory = specWriter.createTestDirectory(projectRootDir)
        specWriter.writeSpecFile(testDirectory, "SampleSpec")

        String resultsId = "DGA423"
        wireMockStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test')
                .withPluginClasspath()
                .build()

        then:
        result.task(":test").outcome == SUCCESS

        and:
        List<LoggedRequest> resultsRequests = wireMockStubber.findResultsRequests()
        resultsRequests.size() == 1

        HttpHeader publishTokenInHeader = resultsRequests[0].header(PUBLISH_TOKEN_NAME)
        publishTokenInHeader.firstValue() == "publish12345"
    }
}
