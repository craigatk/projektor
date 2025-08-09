package projektor.plugin.testkit.coverage

import projektor.plugin.coverage.model.CoverageFilePayload
import projektor.plugin.results.grouped.GroupedResults
import projektor.plugin.testkit.MultiProjectSpec

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static projektor.plugin.CodeUnderTestWriter.writeKotlinSourceCodeFile
import static projektor.plugin.CodeUnderTestWriter.writePartialCoverageKotestFile
import static projektor.plugin.ProjectDirectoryWriter.createKotlinSourceDirectory

class KoverCoverageMultiProjectSpec extends MultiProjectSpec {
    File sourceDirectory1
    File sourceDirectory2
    File sourceDirectory3

    @Override
    boolean includeKoverPlugin() {
        return true
    }

    def setup() {
        sourceDirectory1 = createKotlinSourceDirectory(projectDir1)
        sourceDirectory2 = createKotlinSourceDirectory(projectDir2)
        sourceDirectory3 = createKotlinSourceDirectory(projectDir3)
    }

    def "should publish coverage from multi-project build to server"() {
        given:
        rootBuildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                alwaysPublishInCI = true
            }
        """.stripIndent()

        String publicId = "COV123"
        resultsStubber.stubResultsPostSuccess(publicId)

        writeKotlinSourceCodeFile(sourceDirectory1)
        writePartialCoverageKotestFile(testDirectory1, "PartialTest1")

        writeKotlinSourceCodeFile(sourceDirectory2)
        writePartialCoverageKotestFile(testDirectory2, "PartialTest2")

        writeKotlinSourceCodeFile(sourceDirectory3)
        writePartialCoverageKotestFile(testDirectory3, "PartialTest3")

        when:
        def result = runSuccessfulBuildInCI('test', 'koverXmlReport', '-i')

        then:
        result.task(":project1:test").outcome == SUCCESS
        result.task(":project1:koverXmlReport").outcome == SUCCESS

        result.task(":project2:test").outcome == SUCCESS
        result.task(":project2:koverXmlReport").outcome == SUCCESS

        result.task(":project3:test").outcome == SUCCESS
        result.task(":project3:koverXmlReport").outcome == SUCCESS

        and:
        List<GroupedResults> resultsRequestBodies = resultsStubber.findResultsRequestBodies()
        resultsRequestBodies.size() == 1

        List<CoverageFilePayload> coverageFilePayloads = resultsRequestBodies[0].coverageFiles
        coverageFilePayloads.size() == 3
    }
}
