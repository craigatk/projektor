package projektor.plugin.testkit

import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.gradle.testkit.runner.GradleRunner

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class PluginAutoPublishSingleProjectSpec extends SingleProjectSpec {

    def "should publish results from test task to server"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        File testDirectory = specWriter.createTestDirectory(projectRootDir)
        specWriter.writeFailingSpecFile(testDirectory, "SampleSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test')
                .withPluginClasspath()
                .buildAndFail()

        then:
        !result.output.contains("Projektor plugin enabled but no server specified")
        result.output.contains("View Projektor report at: ${serverUrl}/tests/${resultsId}")

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1
    }

    def "when auto-publish is disabled should not send results to server"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                autoPublish = false
            }
        """.stripIndent()

        File testDirectory = specWriter.createTestDirectory(projectRootDir)
        specWriter.writeFailingSpecFile(testDirectory, "SampleSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test', '-i')
                .withPluginClasspath()
                .buildAndFail()

        then:
        result.output.contains("Projektor plugin auto-publish disabled")
        !result.output.contains("Projektor plugin enabled but no server specified")
        !result.output.contains("View Projektor report at")

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 0
    }

    def "when Projektor plugin is enabled but no server URL set should not send results to server"() {
        given:
        buildFile << """
            projektor {
                serverUrl = null
                autoPublishOnFailureOnly = false
            }
        """.stripIndent()

        File testDirectory = specWriter.createTestDirectory(projectRootDir)
        specWriter.writeSpecFile(testDirectory, "SampleSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test')
                .withPluginClasspath()
                .build()

        then:
        result.task(":test").outcome == SUCCESS

        and:
        result.output.contains("Projektor plugin enabled but no server specified")
        !result.output.contains("View Projektor report at")

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 0
    }
}
