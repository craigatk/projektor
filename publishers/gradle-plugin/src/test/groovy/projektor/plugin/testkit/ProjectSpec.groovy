package projektor.plugin.testkit

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import projektor.plugin.CoverageWireMockStubber
import projektor.plugin.ResultsWireMockStubber
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

abstract class ProjectSpec extends Specification {
    @Rule
    TemporaryFolder projectRootDir = new TemporaryFolder()

    @Rule
    WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort())

    CoverageWireMockStubber coverageStubber = new CoverageWireMockStubber(wireMockRule)
    ResultsWireMockStubber resultsStubber = new ResultsWireMockStubber(wireMockRule)

    String serverUrl

    def setup() {
        serverUrl = resultsStubber.serverUrl
    }

    BuildResult runSuccessfulBuild(String... buildArgs) {
        BuildResult result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments(buildArgs)
                .withPluginClasspath()
                .build()

        println result.output

        return result
    }

    BuildResult runFailedBuild(String... buildArgs) {
        BuildResult result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments(buildArgs)
                .withPluginClasspath()
                .buildAndFail()

        println result.output

        return result
    }
}
