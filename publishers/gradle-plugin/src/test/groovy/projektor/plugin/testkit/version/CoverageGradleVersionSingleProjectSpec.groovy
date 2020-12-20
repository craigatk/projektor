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
            }
        """.stripIndent()

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        File sourceDir = createSourceDirectory(projectRootDir)
        File testDir = createTestDirectory(projectRootDir)

        File sourceFile = writeSourceCodeFile(sourceDir)
        writePartialCoverageSpecFile(testDir, "PartialSpec")

        when:
        def result = runSuccessfulBuildInCI('test', 'jacocoTestReport')

        then:
        result.task(":test").outcome == SUCCESS
        result.task(":jacocoTestReport").outcome == SUCCESS

        and:
        verifyOutputContainsReportLink(result.output, serverUrl, resultsId)

        and:
        List<GroupedResults> resultsRequestBodies = resultsStubber.findResultsRequestBodies()
        resultsRequestBodies.size() == 1

        List<CoverageFilePayload> coverageFilePayloads = resultsRequestBodies[0].coverageFiles
        coverageFilePayloads.size() == 1

        coverageFilePayloads[0].reportContents.contains("report.dtd")
        coverageFilePayloads[0].reportContents.contains(sourceFile.name)

        where:
        gradleVersion                   | _
        "5.0"                           | _
        "6.0.1"                         | _
        "6.4.1"                         | _
        GradleVersion.current().version | _
    }
}
