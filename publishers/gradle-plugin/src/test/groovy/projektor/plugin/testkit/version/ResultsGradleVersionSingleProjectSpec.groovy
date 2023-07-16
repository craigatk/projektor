package projektor.plugin.testkit.version

import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import projektor.plugin.SpecWriter
import projektor.plugin.testkit.SingleProjectSpec
import spock.lang.Unroll

import static projektor.plugin.PluginOutput.verifyOutputContainsReportLink

class ResultsGradleVersionSingleProjectSpec extends SingleProjectSpec {
    @Unroll
    def "should publish results from test task to server with Gradle version #gradleVersion"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithFailingTest(projectRootDir, "SampleSpec")

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
        verifyOutputContainsReportLink(result.output, serverUrl, resultsId)

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1

        where:
        gradleVersion                          | _
        GradleVersion.version("7.6.1").version | _
        GradleVersion.current().version        | _
    }
}
