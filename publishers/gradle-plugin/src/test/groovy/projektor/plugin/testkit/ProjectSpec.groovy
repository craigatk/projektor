package projektor.plugin.testkit

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import projektor.plugin.ResultsWireMockStubber
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

abstract class ProjectSpec extends Specification {
    @Rule
    TemporaryFolder projectRootDir = new TemporaryFolder()

    @Rule
    WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort())

    ResultsWireMockStubber resultsStubber = new ResultsWireMockStubber(wireMockRule)

    String serverUrl

    def setup() {
        serverUrl = resultsStubber.serverUrl
    }

    BuildResult runSuccessfulLocalBuild(String... buildArgs) {
        runSuccessfulBuildWithEnvironment(["CI": "false"], buildArgs)
    }

    BuildResult runSuccessfulBuildInCI(String... buildArgs) {
        runSuccessfulBuildWithEnvironment(["CI": "true"], buildArgs)
    }

    BuildResult runSuccessfulBuildWithEnvironment(Map<String, String> envMap, String... buildArgs) {
        Map<String, String> currentEnv = System.getenv()
        Map<String, String> augmentedEnv = new HashMap<>(currentEnv)
        augmentedEnv.putAll(envMap)

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withEnvironment(augmentedEnv)
                .withArguments(buildArgs)
                .withPluginClasspath()
                .build()

        println result.output

        return result
    }

    BuildResult runSuccessfulBuildWithEnvironmentAndGradleVersion(Map<String, String> envMap, GradleVersion gradleVersion, String ... buildArgs) {
        Map<String, String> currentEnv = System.getenv()
        Map<String, String> augmentedEnv = new HashMap<>(currentEnv)
        augmentedEnv.putAll(envMap)

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withEnvironment(augmentedEnv)
                .withArguments(buildArgs)
                .withPluginClasspath()
                .withGradleVersion(gradleVersion.version)
                .build()

        println result.output

        return result
    }

    BuildResult runFailedBuildInCI(String... buildArgs) {
        runFailedBuildWithEnvironment(["CI": "true"], buildArgs)
    }

    BuildResult runFailedBuildWithEnvironment(Map<String, String> envMap, String... buildArgs) {
        Map<String, String> currentEnv = System.getenv()
        Map<String, String> augmentedEnv = new HashMap<>(currentEnv)
        augmentedEnv.putAll(envMap)

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withEnvironment(augmentedEnv)
                .withArguments(buildArgs)
                .withPluginClasspath()
                .buildAndFail()

        println result.output

        return result
    }

    BuildResult runFailedLocalBuild(String... buildArgs) {
        runFailedBuildWithEnvironment(["CI": "false"], buildArgs)
    }

    boolean includeJacocoPlugin() {
        return false
    }

    boolean includeKoverPlugin() {
        return false
    }

    String koverPluginVersion() {
        return "0.9.1"
    }

    boolean includeCodenarcPlugin() {
        return false
    }
}
