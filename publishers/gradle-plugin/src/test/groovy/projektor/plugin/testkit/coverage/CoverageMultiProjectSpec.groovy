package projektor.plugin.testkit.coverage

import projektor.plugin.coverage.model.CoverageFilePayload
import projektor.plugin.results.grouped.GroupedResults
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
                alwaysPublishInCI = true
            }
        """.stripIndent()

        String publicId = "COV123"
        resultsStubber.stubResultsPostSuccess(publicId)

        writeSourceCodeFile(sourceDirectory1)
        writePartialCoverageSpecFile(testDirectory1, "PartialSpec1")

        writeSourceCodeFile(sourceDirectory2)
        writePartialCoverageSpecFile(testDirectory2, "PartialSpec2")

        writeSourceCodeFile(sourceDirectory3)
        writePartialCoverageSpecFile(testDirectory3, "PartialSpec3")

        when:
        def result = runSuccessfulBuildInCI('test', 'jacocoTestReport', '-i')

        then:
        result.task(":project1:test").outcome == SUCCESS
        result.task(":project1:jacocoTestReport").outcome == SUCCESS

        result.task(":project2:test").outcome == SUCCESS
        result.task(":project2:jacocoTestReport").outcome == SUCCESS

        result.task(":project3:test").outcome == SUCCESS
        result.task(":project3:jacocoTestReport").outcome == SUCCESS

        and:
        List<GroupedResults> resultsRequestBodies = resultsStubber.findResultsRequestBodies()
        resultsRequestBodies.size() == 1

        List<CoverageFilePayload> coverageFilePayloads = resultsRequestBodies[0].coverageFiles
        coverageFilePayloads.size() == 3
    }

    def "should include full directory path in published coverage data"() {
        given:
        rootBuildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                alwaysPublishInCI = true
            }
        """.stripIndent()

        String publicId = "COV123"
        resultsStubber.stubResultsPostSuccess(publicId)

        writeSourceCodeFile(sourceDirectory1)
        writePartialCoverageSpecFile(testDirectory1, "PartialSpec1")

        writeSourceCodeFile(sourceDirectory2)
        writePartialCoverageSpecFile(testDirectory2, "PartialSpec2")

        writeSourceCodeFile(sourceDirectory3)
        writePartialCoverageSpecFile(testDirectory3, "PartialSpec3")

        when:
        def result = runSuccessfulBuildInCI('test', 'jacocoTestReport', '-i')

        then:
        result.task(":project1:test").outcome == SUCCESS
        result.task(":project1:jacocoTestReport").outcome == SUCCESS

        result.task(":project2:test").outcome == SUCCESS
        result.task(":project2:jacocoTestReport").outcome == SUCCESS

        result.task(":project3:test").outcome == SUCCESS
        result.task(":project3:jacocoTestReport").outcome == SUCCESS

        and:
        List<GroupedResults> resultsRequestBodies = resultsStubber.findResultsRequestBodies()
        resultsRequestBodies.size() == 1

        List<CoverageFilePayload> coverageFilePayloads = resultsRequestBodies[0].coverageFiles
        coverageFilePayloads.size() == 3

        coverageFilePayloads.find { it.baseDirectoryPath == "project1/src/main/groovy"}
        coverageFilePayloads.find { it.baseDirectoryPath == "project2/src/main/groovy"}
        coverageFilePayloads.find { it.baseDirectoryPath == "project3/src/main/groovy"}
    }

    def "when only one subproject changes should publish coverage from all subprojects"() {
        given:
        rootBuildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                alwaysPublishInCI = true
            }
        """.stripIndent()

        String publicId1 = "COVER1"
        resultsStubber.stubResultsPostSuccess(publicId1)

        writeSourceCodeFile(sourceDirectory1)
        writePartialCoverageSpecFile(testDirectory1, "PartialSpec1")

        writeSourceCodeFile(sourceDirectory2)
        writePartialCoverageSpecFile(testDirectory2, "PartialSpec2")

        writeSourceCodeFile(sourceDirectory3)
        writePartialCoverageSpecFile(testDirectory3, "PartialSpec3")

        when:
        def result1 = runSuccessfulBuildInCI('test', 'jacocoTestReport', '-i')

        then:
        result1.task(":project1:test").outcome == SUCCESS
        result1.task(":project1:jacocoTestReport").outcome == SUCCESS

        result1.task(":project2:test").outcome == SUCCESS
        result1.task(":project2:jacocoTestReport").outcome == SUCCESS

        result1.task(":project3:test").outcome == SUCCESS
        result1.task(":project3:jacocoTestReport").outcome == SUCCESS

        and:
        List<GroupedResults> resultsRequestBodies1 = resultsStubber.findResultsRequestBodies()
        resultsRequestBodies1.size() == 1

        List<CoverageFilePayload> coverageFilePayloads1 = resultsRequestBodies1[0].coverageFiles
        coverageFilePayloads1.size() == 3

        when:
        wireMockRule.resetAll()

        String publicId2 = "COVER2"
        resultsStubber.stubResultsPostSuccess(publicId2)

        writePartialCoverageSpecFile(testDirectory2, "PartialSpec4")

        def result2 = runSuccessfulBuildInCI('test', 'jacocoTestReport', '-i')

        then:
        result2.task(":project1:test").outcome == UP_TO_DATE
        result2.task(":project1:jacocoTestReport").outcome == UP_TO_DATE

        result2.task(":project2:test").outcome == SUCCESS
        result2.task(":project2:jacocoTestReport").outcome == SUCCESS

        result2.task(":project3:test").outcome == UP_TO_DATE
        result2.task(":project3:jacocoTestReport").outcome == UP_TO_DATE

        and:
        List<GroupedResults> resultsRequestBodies2 = resultsStubber.findResultsRequestBodies()
        resultsRequestBodies2.size() == 1

        List<CoverageFilePayload> coverageFilePayloads2 = resultsRequestBodies2[0].coverageFiles
        coverageFilePayloads2.size() == 3
    }

    def "when no subprojects change and in CI should publish coverage from all subprojects"() {
        given:
        rootBuildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                alwaysPublishInCI = true
            }
        """.stripIndent()

        String publicId1 = "COVER1"
        resultsStubber.stubResultsPostSuccess(publicId1)

        writeSourceCodeFile(sourceDirectory1)
        writePartialCoverageSpecFile(testDirectory1, "PartialSpec1")

        writeSourceCodeFile(sourceDirectory2)
        writePartialCoverageSpecFile(testDirectory2, "PartialSpec2")

        writeSourceCodeFile(sourceDirectory3)
        writePartialCoverageSpecFile(testDirectory3, "PartialSpec3")

        when:
        def result1 = runSuccessfulBuildWithEnvironment(["CI": "true"], 'test', 'jacocoTestReport')

        then:
        result1.task(":project1:test").outcome == SUCCESS
        result1.task(":project1:jacocoTestReport").outcome == SUCCESS

        result1.task(":project2:test").outcome == SUCCESS
        result1.task(":project2:jacocoTestReport").outcome == SUCCESS

        result1.task(":project3:test").outcome == SUCCESS
        result1.task(":project3:jacocoTestReport").outcome == SUCCESS

        and:
        List<GroupedResults> resultsRequestBodies1 = resultsStubber.findResultsRequestBodies()
        resultsRequestBodies1.size() == 1

        List<CoverageFilePayload> coverageFilePayloads1 = resultsRequestBodies1[0].coverageFiles
        coverageFilePayloads1.size() == 3

        when:
        wireMockRule.resetAll()

        String publicId2 = "COVER2"
        resultsStubber.stubResultsPostSuccess(publicId2)

        def result2 = runSuccessfulBuildWithEnvironment(["CI": "true"], 'test', 'jacocoTestReport', '-i')

        then:
        result2.task(":project1:test").outcome == UP_TO_DATE
        result2.task(":project1:jacocoTestReport").outcome == UP_TO_DATE

        result2.task(":project2:test").outcome == UP_TO_DATE
        result2.task(":project2:jacocoTestReport").outcome == UP_TO_DATE

        result2.task(":project3:test").outcome == UP_TO_DATE
        result2.task(":project3:jacocoTestReport").outcome == UP_TO_DATE

        and:
        List<GroupedResults> resultsRequestBodies2 = resultsStubber.findResultsRequestBodies()
        resultsRequestBodies2.size() == 1

        List<CoverageFilePayload> coverageFilePayloads2 = resultsRequestBodies2[0].coverageFiles
        coverageFilePayloads2.size() == 3
    }
}
