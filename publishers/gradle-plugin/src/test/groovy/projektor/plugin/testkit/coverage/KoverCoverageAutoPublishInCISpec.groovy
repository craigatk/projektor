package projektor.plugin.testkit.coverage

import projektor.plugin.results.grouped.GroupedResults
import projektor.plugin.testkit.SingleProjectSpec

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static projektor.plugin.CodeUnderTestWriter.writeKotlinSourceCodeFile
import static projektor.plugin.CodeUnderTestWriter.writePartialCoverageKotestFile
import static projektor.plugin.CodeUnderTestWriter.writePartialCoverageSpecFile
import static projektor.plugin.CodeUnderTestWriter.writeSourceCodeFile
import static projektor.plugin.ProjectDirectoryWriter.createKotlinSourceDirectory
import static projektor.plugin.ProjectDirectoryWriter.createKotlinTestDirectory
import static projektor.plugin.ProjectDirectoryWriter.createSourceDirectory
import static projektor.plugin.ProjectDirectoryWriter.createTestDirectory

class KoverCoverageAutoPublishInCISpec extends SingleProjectSpec {
    @Override
    boolean includeKoverPlugin() {
        return true
    }

    def "should publish results and coverage when publish in CI enabled and coverage exists"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        String publicId = "AUTOCOV123"
        resultsStubber.stubResultsPostSuccess(publicId)

        File sourceDir = createKotlinSourceDirectory(projectRootDir)
        File testDir = createKotlinTestDirectory(projectRootDir)

        writeKotlinSourceCodeFile(sourceDir)
        writePartialCoverageKotestFile(testDir, "PartialTest")

        when:
        def result = runSuccessfulBuildWithEnvironment(["CI": "true"], 'test', 'koverXmlReport')

        then:
        result.task(":test").outcome == SUCCESS
        result.task(":koverXmlReport").outcome == SUCCESS

        and:
        List<GroupedResults> resultRequestBodies = resultsStubber.findResultsRequestBodies()
        resultRequestBodies.size() == 1

        resultRequestBodies[0].coverageFiles.size() == 1
    }

    def "should not publish results and coverage when local and running non-test task"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        String publicId = "AUTOCOV123"
        resultsStubber.stubResultsPostSuccess(publicId)

        File sourceDir = createKotlinSourceDirectory(projectRootDir)
        File testDir = createKotlinTestDirectory(projectRootDir)

        writeKotlinSourceCodeFile(sourceDir)
        writePartialCoverageKotestFile(testDir, "PartialTest")

        when:
        def result = runSuccessfulLocalBuild('tasks')

        then:
        result.task(":tasks").outcome == SUCCESS
        !result.task(":koverXmlReport")
        !result.task(":test")

        and:
        resultsStubber.findResultsRequests().size() == 0
    }
}
