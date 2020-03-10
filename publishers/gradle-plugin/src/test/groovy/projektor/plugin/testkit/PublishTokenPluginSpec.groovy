package projektor.plugin.testkit

import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.gradle.testkit.runner.GradleRunner
import projektor.plugin.client.ClientToken

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
        specWriter.writeFailingSpecFile(testDirectory, "SampleSpec")

        String resultsId = "DGA423"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test')
                .withPluginClasspath()
                .buildAndFail()

        then:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1

        HttpHeader publishTokenInHeader = resultsRequests[0].header(ClientToken.PUBLISH_TOKEN_NAME)
        publishTokenInHeader.firstValue() == "publish12345"
    }
}
