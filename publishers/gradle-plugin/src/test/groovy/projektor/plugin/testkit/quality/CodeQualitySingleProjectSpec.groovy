package projektor.plugin.testkit.quality

import projektor.plugin.CodenarcWriter
import projektor.plugin.results.grouped.GroupedResults
import projektor.plugin.testkit.SingleProjectSpec

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static projektor.plugin.CodeUnderTestWriter.writePartialCoverageSpecFile
import static projektor.plugin.CodeUnderTestWriter.writeSourceCodeFile
import static projektor.plugin.ProjectDirectoryWriter.createSourceDirectory
import static projektor.plugin.ProjectDirectoryWriter.createTestDirectory

class CodeQualitySingleProjectSpec extends SingleProjectSpec {

    @Override
    boolean includeCodenarcPlugin() {
        return true
    }

    def "should post code quality reports"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                codeQualityReports = [fileTree(dir: 'build/reports/codenarc/', include: '*.txt')]
            }
        """.stripIndent()

        CodenarcWriter.writeCodenarcConfigFile(projectRootDir)

        String publicId = "COV123"
        resultsStubber.stubResultsPostSuccess(publicId)

        File sourceDir = createSourceDirectory(projectRootDir)
        File testDir = createTestDirectory(projectRootDir)

        writeSourceCodeFile(sourceDir)
        writePartialCoverageSpecFile(testDir, "PartialSpec")

        when:
        def result = runFailedBuildInCI('codenarcTest', '-i')

        then:
        result.task(":codenarcTest").outcome == FAILED

        and:
        List<GroupedResults> resultsRequestBodies = resultsStubber.findResultsRequestBodies()
        resultsRequestBodies.size() == 1

        GroupedResults groupedResults = resultsRequestBodies[0]
        groupedResults.codeQualityFiles.size() == 1

        groupedResults.codeQualityFiles[0].contents.contains("CodeNarc Report")
        groupedResults.codeQualityFiles[0].fileName == "test.txt"
    }
}
