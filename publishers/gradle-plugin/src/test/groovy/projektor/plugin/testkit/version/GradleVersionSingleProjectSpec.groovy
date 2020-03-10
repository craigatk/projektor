package projektor.plugin.testkit.version

import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import projektor.plugin.testkit.SingleProjectSpec
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class GradleVersionSingleProjectSpec extends SingleProjectSpec {
    @Unroll
    def "should publish results from test task to server with Gradle version #gradleVersion"() {
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
                .withGradleVersion(gradleVersion)
                .buildAndFail()

        then:
        !result.output.contains("Projektor plugin enabled but no server specified")
        result.output.contains("View Projektor report at: ${serverUrl}/tests/${resultsId}")

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1

        where:
        gradleVersion                   | _
        "4.0"                           | _
        "5.0"                           | _
        "6.0.1"                         | _
        GradleVersion.current().version | _
    }
}
