package projektor.plugin.testkit.task

import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.gradle.testkit.runner.GradleRunner
import projektor.plugin.testkit.SingleProjectSpec

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class PublishResultsTaskSingleProjectSpec extends SingleProjectSpec {

    def "should publish results with publish task"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                autoPublish = false
            }
        """.stripIndent()

        File testDirectory = specWriter.createTestDirectory(projectRootDir)
        specWriter.writeSpecFile(testDirectory, "SampleSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def testResult = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test')
                .withPluginClasspath()
                .build()

        then:
        testResult.task(":test").outcome == SUCCESS

        and:
        resultsStubber.findResultsRequests().size() == 0

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
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1

        and:
        String requestBody = resultsRequests[0].bodyAsString

        requestBody.contains('SampleSpec')
        requestBody.contains('sample test')
    }

}
