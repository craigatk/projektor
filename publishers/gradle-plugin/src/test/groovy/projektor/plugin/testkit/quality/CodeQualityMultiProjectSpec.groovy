package projektor.plugin.testkit.quality

import projektor.plugin.CodenarcWriter
import projektor.plugin.quality.CodeQualityFilePayload
import projektor.plugin.results.grouped.GroupedResults
import projektor.plugin.testkit.MultiProjectSpec

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static projektor.plugin.CodeUnderTestWriter.writePartialCoverageSpecFile
import static projektor.plugin.CodeUnderTestWriter.writeSourceCodeFile
import static projektor.plugin.ProjectDirectoryWriter.createSourceDirectory

class CodeQualityMultiProjectSpec extends MultiProjectSpec {
    File sourceDirectory1
    File sourceDirectory2
    File sourceDirectory3

    @Override
    boolean includeCodenarcPlugin() {
        return true
    }

    def setup() {
        sourceDirectory1 = createSourceDirectory(projectDir1)
        sourceDirectory2 = createSourceDirectory(projectDir2)
        sourceDirectory3 = createSourceDirectory(projectDir3)
    }

    def "should write code quality reports from multiple subprojects"() {
        given:
        rootBuildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                codeQualityReports = [fileTree(dir: ".", include: '**/build/reports/codenarc/*.txt')]
            }
        """.stripIndent()

        CodenarcWriter.writeCodenarcConfigFile(projectRootDir)
        CodenarcWriter.writeCodenarcConfigFile(projectDir1)
        CodenarcWriter.writeCodenarcConfigFile(projectDir2)
        CodenarcWriter.writeCodenarcConfigFile(projectDir3)

        String publicId = "COV123"
        resultsStubber.stubResultsPostSuccess(publicId)

        writeSourceCodeFile(sourceDirectory1)
        writePartialCoverageSpecFile(testDirectory1, "PartialSpec1")

        writeSourceCodeFile(sourceDirectory2)
        writePartialCoverageSpecFile(testDirectory2, "PartialSpec2")

        writeSourceCodeFile(sourceDirectory3)
        writePartialCoverageSpecFile(testDirectory3, "PartialSpec3")

        when:
        def result = runFailedBuildInCI('codenarcTest', '-i', '--continue')

        then:
        result.task(":project1:codenarcTest").outcome == FAILED
        result.task(":project2:codenarcTest").outcome == FAILED
        result.task(":project3:codenarcTest").outcome == FAILED

        and:
        List<GroupedResults> resultsRequestBodies = resultsStubber.findResultsRequestBodies()
        resultsRequestBodies.size() == 1

        GroupedResults groupedResults = resultsRequestBodies[0]
        groupedResults.codeQualityFiles.size() == 3

        List<CodeQualityFilePayload> codeQualityFiles = groupedResults.codeQualityFiles
        codeQualityFiles.find { it.fileName == "test.txt" && it.contents.contains("PartialSpec1")}
        codeQualityFiles.find { it.fileName == "test.txt" && it.contents.contains("PartialSpec2")}
        codeQualityFiles.find { it.fileName == "test.txt" && it.contents.contains("PartialSpec3")}
    }
}
