package projektor.plugin.testkit

import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.gradle.testkit.runner.GradleRunner

import static org.gradle.testkit.runner.TaskOutcome.*

class PluginAutoPublishTestTaskNotExecutedSpec extends SingleProjectSpec {

    def "when no test tasks executed should not publish Projektor report"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        File testDirectory = specWriter.createTestDirectory(projectRootDir)
        specWriter.writeSpecFile(testDirectory, "SampleSpec")

        String resultsId = "ABC123"
        wireMockStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('compileTestGroovy')
                .withPluginClasspath()
                .build()

        then:
        result.task(":compileTestGroovy").outcome == SUCCESS

        and:
        !result.output.contains("Projektor plugin enabled but no server specified")
        !result.output.contains("View Projektor report at: ${serverUrl}/tests/${resultsId}")

        and:
        List<LoggedRequest> resultsRequests = wireMockStubber.findResultsRequests()
        resultsRequests.size() == 0
    }

    def "when test task was up-to-date should not publish results"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        File testDirectory = specWriter.createTestDirectory(projectRootDir)
        specWriter.writeSpecFile(testDirectory, "SampleSpec")

        String resultsId = "ABC123"
        wireMockStubber.stubResultsPostSuccess(resultsId)

        when:
        def executedResult = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test')
                .withPluginClasspath()
                .build()

        then:
        executedResult.task(":test").outcome == SUCCESS

        and:
        executedResult.output.contains("View Projektor report at")

        and:
        wireMockStubber.findResultsRequests().size() == 1

        when:
        wireMockRule.resetRequests()

        def upToDateResult = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test')
                .withPluginClasspath()
                .build()

        then:
        upToDateResult.task(":test").outcome == UP_TO_DATE

        and:
        !upToDateResult.output.contains("View Projektor report at")

        and:
        wireMockStubber.findResultsRequests().size() == 0
    }

    def "when test task was skipped should not publish results"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        File testDirectory = specWriter.createTestDirectory(projectRootDir)
        specWriter.writeSpecFile(testDirectory, "SampleSpec")

        String resultsId = "ABC123"
        wireMockStubber.stubResultsPostSuccess(resultsId)

        when:
        def executedBuildResult = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test')
                .withPluginClasspath()
                .build()

        then:
        executedBuildResult.task(":test").outcome == SUCCESS

        and:
        executedBuildResult.output.contains("View Projektor report at")

        and:
        wireMockStubber.findResultsRequests().size() == 1

        when:
        wireMockRule.resetRequests()

        buildFile << """
            test.onlyIf { false }
        """.stripIndent()

        def skippedBuildResult = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('check')
                .withPluginClasspath()
                .build()

        then:
        skippedBuildResult.task(":test").outcome == SKIPPED

        and:
        !skippedBuildResult.output.contains("View Projektor report at")

        and:
        wireMockStubber.findResultsRequests().size() == 0
    }
}
