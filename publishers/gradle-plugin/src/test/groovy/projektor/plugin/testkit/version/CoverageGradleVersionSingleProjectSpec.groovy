package projektor.plugin.testkit.version

import org.gradle.util.GradleVersion
import projektor.plugin.coverage.model.CoverageFilePayload
import projektor.plugin.results.grouped.GroupedResults
import projektor.plugin.testkit.SingleProjectSpec
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static projektor.plugin.CodeUnderTestWriter.writePartialCoverageSpecFile
import static projektor.plugin.CodeUnderTestWriter.writeSourceCodeFile
import static projektor.plugin.PluginOutput.verifyOutputContainsReportLink
import static projektor.plugin.ProjectDirectoryWriter.createSourceDirectory
import static projektor.plugin.ProjectDirectoryWriter.createTestDirectory

class CoverageGradleVersionSingleProjectSpec extends SingleProjectSpec {
    @Override
    boolean includeJacocoPlugin() {
        return true
    }

    @Unroll
    def "should publish results and coverage with Gradle version #gradleVersion"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                alwaysPublish = true
            }
        """.stripIndent()

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        File sourceDir = createSourceDirectory(projectRootDir)
        File testDir = createTestDirectory(projectRootDir)

        File sourceFile = writeSourceCodeFile(sourceDir)
        writePartialCoverageSpecFile(testDir, "PartialSpec")

        when:
        def result = runSuccessfulBuildWithEnvironmentAndGradleVersion(
                ["CI": "true"],
                gradleVersion,
                'test', 'jacocoTestReport', '--info', '--warning-mode', 'all'
        )

        then:
        result.task(":test").outcome == SUCCESS
        result.task(":jacocoTestReport").outcome == SUCCESS

        and:
        verifyOutputContainsReportLink(result.output, serverUrl, resultsId)

        and:
        !result.output.contains("scheduled to be removed in Gradle 8.0")

        and:
        List<GroupedResults> resultsRequestBodies = resultsStubber.findResultsRequestBodies()
        resultsRequestBodies.size() == 1

        List<CoverageFilePayload> coverageFilePayloads = resultsRequestBodies[0].coverageFiles
        coverageFilePayloads.size() == 1

        coverageFilePayloads[0].reportContents.contains("report.dtd")
        coverageFilePayloads[0].reportContents.contains(sourceFile.name)

        coverageFilePayloads[0].baseDirectoryPath == "src/main/groovy"

        where:
        gradleVersion                  | _
        GradleVersion.version("7.6.1") | _
        GradleVersion.current()        | _
    }
}
