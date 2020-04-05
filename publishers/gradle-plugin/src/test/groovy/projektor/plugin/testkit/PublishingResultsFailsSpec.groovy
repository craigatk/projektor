package projektor.plugin.testkit

import org.gradle.testkit.runner.BuildResult
import projektor.plugin.SpecWriter

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class PublishingResultsFailsSpec extends SingleProjectSpec {
    def "when publishing fails with networking error to server should not fail build"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithPassingTest(projectRootDir, "SampleSpec")

        resultsStubber.stubResultsNetworkingError()

        when:
        BuildResult result = runSuccessfulBuild('test')

        then:
        result.task(":test").outcome == SUCCESS
    }

    def "when publishing fails with non-200 response should not fail build"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithPassingTest(projectRootDir, "SampleSpec")

        resultsStubber.stubResultsPostFailure(400)

        when:
        BuildResult result = runSuccessfulBuild('test')

        then:
        result.task(":test").outcome == SUCCESS
    }
}
