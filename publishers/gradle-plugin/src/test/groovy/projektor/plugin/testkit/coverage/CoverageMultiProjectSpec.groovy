package projektor.plugin.testkit.coverage

import projektor.plugin.testkit.MultiProjectSpec

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
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

    def "when only one subproject changes should publish coverage from all subprojects"() {
        given:
        rootBuildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                autoPublishOnFailureOnly = false
            }
        """.stripIndent()

        String publicId1 = "COVER1"
        resultsStubber.stubResultsPostSuccess(publicId1)

        coverageStubber.stubCoveragePostSuccess(publicId1)

        writeSourceCodeFile(sourceDirectory1)
        writePartialCoverageSpecFile(testDirectory1, "PartialSpec1")

        writeSourceCodeFile(sourceDirectory2)
        writePartialCoverageSpecFile(testDirectory2, "PartialSpec2")

        writeSourceCodeFile(sourceDirectory3)
        writePartialCoverageSpecFile(testDirectory3, "PartialSpec3")

        when:
        def result1 = runSuccessfulBuild('test', 'jacocoTestReport', '-i')

        then:
        result1.task(":project1:test").outcome == SUCCESS
        result1.task(":project1:jacocoTestReport").outcome == SUCCESS

        result1.task(":project2:test").outcome == SUCCESS
        result1.task(":project2:jacocoTestReport").outcome == SUCCESS

        result1.task(":project3:test").outcome == SUCCESS
        result1.task(":project3:jacocoTestReport").outcome == SUCCESS

        and:
        coverageStubber.findCoverageRequests(publicId1).size() == 3

        when:
        wireMockRule.resetAll()

        String publicId2 = "COVER2"
        resultsStubber.stubResultsPostSuccess(publicId2)

        coverageStubber.stubCoveragePostSuccess(publicId2)

        writePartialCoverageSpecFile(testDirectory2, "PartialSpec4")

        def result2 = runSuccessfulBuild('test', 'jacocoTestReport', '-i')

        then:
        result2.task(":project1:test").outcome == UP_TO_DATE
        result2.task(":project1:jacocoTestReport").outcome == UP_TO_DATE

        result2.task(":project2:test").outcome == SUCCESS
        result2.task(":project2:jacocoTestReport").outcome == SUCCESS

        result2.task(":project3:test").outcome == UP_TO_DATE
        result2.task(":project3:jacocoTestReport").outcome == UP_TO_DATE

        and:
        coverageStubber.findCoverageRequests(publicId2).size() == 3
    }
}
