package projektor.plugin.testkit.coverage

import org.gradle.testkit.runner.BuildResult
import org.gradle.util.GradleVersion
import projektor.plugin.coverage.model.CoverageFilePayload
import projektor.plugin.results.grouped.GroupedResults
import projektor.plugin.testkit.SingleProjectSpec
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static projektor.plugin.CodeUnderTestWriter.*
import static projektor.plugin.ProjectDirectoryWriter.*

class Kover07CoverageSingleProjectSpec extends SingleProjectSpec {

    @Override
    boolean includeKoverPlugin() {
        return true
    }

    @Override
    String koverPluginVersion() {
        return "0.7.2"
    }

    @Unroll
    def "should publish Kover coverage results to server with Gradle version #gradleVersion"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        String publicId = "COV123"
        resultsStubber.stubResultsPostSuccess(publicId)

        File sourceDir = createKotlinSourceDirectory(projectRootDir)
        File testDir = createKotlinTestDirectory(projectRootDir)

        writeKotlinSourceCodeFile(sourceDir)
        writePartialCoverageKotestFile(testDir, "FooTest")

        when:
        BuildResult result = runSuccessfulBuildWithEnvironmentAndGradleVersion(
                ["CI": "true"],
                gradleVersion,
                'test', 'koverXmlReport', '-i'
        )

        then:
        result.task(":test").outcome == SUCCESS
        result.task(":koverXmlReport").outcome == SUCCESS

        and:
        List<GroupedResults> resultsRequestBodies = resultsStubber.findResultsRequestBodies()
        resultsRequestBodies.size() == 1

        List<CoverageFilePayload> coverageFilePayloads = resultsRequestBodies[0].coverageFiles
        coverageFilePayloads.size() == 1

        coverageFilePayloads[0].reportContents.contains("Foo.kt")
        coverageFilePayloads[0].baseDirectoryPath == "src/main/kotlin"

        where:
        gradleVersion                  | _
        GradleVersion.version("7.6.1") | _
        GradleVersion.current()        | _
    }
}
