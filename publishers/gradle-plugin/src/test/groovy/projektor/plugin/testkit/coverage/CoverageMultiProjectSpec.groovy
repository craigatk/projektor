package projektor.plugin.testkit.coverage

import projektor.plugin.testkit.MultiProjectSpec

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static projektor.plugin.CodeUnderTestWriter.writePartialCoverageSpecFile
import static projektor.plugin.CodeUnderTestWriter.writeSourceCodeFile
import static projektor.plugin.ProjectDirectoryWriter.createSourceDirectory

class CoverageMultiProjectSpec extends MultiProjectSpec {
    File sourceDirectory1
    File sourceDirectory2
    File sourceDirectory3

    @Override
    boolean includeJacocoPlugin() {
        return true
    }

    def setup() {
        sourceDirectory1 = createSourceDirectory(projectDir1)
        sourceDirectory2 = createSourceDirectory(projectDir2)
        sourceDirectory3 = createSourceDirectory(projectDir3)
    }

    def "should publish coverage from multi-project build to server"() {
        given:
        rootBuildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                autoPublishOnFailureOnly = false
            }
        """.stripIndent()

        String publicId = "COV123"
        resultsStubber.stubResultsPostSuccess(publicId)

        coverageStubber.stubCoveragePostSuccess(publicId)

        writeSourceCodeFile(sourceDirectory1)
        writePartialCoverageSpecFile(testDirectory1, "PartialSpec1")

        writeSourceCodeFile(sourceDirectory2)
        writePartialCoverageSpecFile(testDirectory2, "PartialSpec2")

        writeSourceCodeFile(sourceDirectory3)
        writePartialCoverageSpecFile(testDirectory3, "PartialSpec3")

        when:
        def result = runSuccessfulBuild('test', 'jacocoTestReport', '-i')

        then:
        result.task(":project1:test").outcome == SUCCESS
        result.task(":project1:jacocoTestReport").outcome == SUCCESS

        result.task(":project2:test").outcome == SUCCESS
        result.task(":project2:jacocoTestReport").outcome == SUCCESS

        result.task(":project3:test").outcome == SUCCESS
        result.task(":project3:jacocoTestReport").outcome == SUCCESS

        and:
        coverageStubber.findCoverageRequests(publicId).size() == 3
    }
}
