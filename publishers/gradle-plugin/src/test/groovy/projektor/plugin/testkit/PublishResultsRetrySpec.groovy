package projektor.plugin.testkit

import org.gradle.testkit.runner.BuildResult
import projektor.plugin.SpecWriter

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class PublishResultsRetrySpec extends SingleProjectSpec {
    def "can configure retry max attempts and interval"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                autoPublishOnFailureOnly = false
                publishRetryMaxAttempts = 2
                publishRetryInterval = 50
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithPassingTest(projectRootDir, "SampleSpec")

        resultsStubber.stubResultsPostFailure(400)

        when:
        BuildResult result = runSuccessfulBuild('test')

        then:
        result.task(":test").outcome == SUCCESS

        and:
        resultsStubber.findResultsRequests().size() == 2
    }

    def "can configure timeout"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                autoPublishOnFailureOnly = false
                publishRetryMaxAttempts = 2
                publishRetryInterval = 0
                publishTimeout = 500
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithPassingTest(projectRootDir, "SampleSpec")

        resultsStubber.stubResultsPostWithDelay(600)

        when:
        BuildResult result = runSuccessfulBuild('test')

        then:
        result.task(":test").outcome == SUCCESS

        and:
        resultsStubber.findResultsRequests().size() == 2
    }
}
