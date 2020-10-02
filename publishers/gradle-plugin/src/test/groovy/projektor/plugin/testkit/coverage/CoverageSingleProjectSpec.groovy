package projektor.plugin.testkit.coverage

import projektor.plugin.testkit.SingleProjectSpec

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static projektor.plugin.CodeUnderTestWriter.writePartialCoverageSpecFile
import static projektor.plugin.CodeUnderTestWriter.writeSourceCodeFile
import static projektor.plugin.ProjectDirectoryWriter.createSourceDirectory
import static projektor.plugin.ProjectDirectoryWriter.createTestDirectory

class CoverageSingleProjectSpec extends SingleProjectSpec {

    @Override
    boolean includeJacocoPlugin() {
        return true
    }

    def "should publish coverage results to server"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        String publicId = "COV123"
        resultsStubber.stubResultsPostSuccess(publicId)

        coverageStubber.stubCoveragePostSuccess(publicId)

        File sourceDir = createSourceDirectory(projectRootDir)
        File testDir = createTestDirectory(projectRootDir)

        writeSourceCodeFile(sourceDir)
        writePartialCoverageSpecFile(testDir, "PartialSpec")

        when:
        def result = runSuccessfulBuildInCI('test', 'jacocoTestReport', '-i')

        then:
        result.task(":test").outcome == SUCCESS
        result.task(":jacocoTestReport").outcome == SUCCESS

        and:
        coverageStubber.findCoverageRequests(publicId).size() == 1
    }

    def "when coverage disabled should not publish coverage results to server"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                codeCoveragePublish = false
            }
        """.stripIndent()

        String publicId = "COV123"
        resultsStubber.stubResultsPostSuccess(publicId)

        coverageStubber.stubCoveragePostSuccess(publicId)

        File sourceDir = createSourceDirectory(projectRootDir)
        File testDir = createTestDirectory(projectRootDir)

        writeSourceCodeFile(sourceDir)
        writePartialCoverageSpecFile(testDir, "PartialSpec")

        when:
        def result = runSuccessfulBuildInCI('test', 'jacocoTestReport', '-i')

        then:
        result.task(":test").outcome == SUCCESS
        result.task(":jacocoTestReport").outcome == SUCCESS

        and:
        coverageStubber.findCoverageRequests(publicId).size() == 0
    }
}
