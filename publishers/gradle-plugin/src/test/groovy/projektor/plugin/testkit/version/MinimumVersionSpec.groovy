package projektor.plugin.testkit.version

import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.gradle.testkit.runner.GradleRunner
import projektor.plugin.SpecWriter
import projektor.plugin.testkit.SingleProjectSpec
import spock.lang.Unroll

class MinimumVersionSpec extends SingleProjectSpec {
    @Unroll
    def "when running with Gradle version #gradleVersion less than Gradle 7.6.1 should fail"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithPassingTest(projectRootDir, "SampleSpec")

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
        result.output.contains("This version of the Projektor Gradle plugin supports Gradle 7.6.1+ only. Please upgrade the version of Gradle your project uses.")

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 0

        where:
        gradleVersion                   | _
        "5.0"                           | _
        "6.0"                           | _
    }
}
