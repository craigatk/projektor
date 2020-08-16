package projektor.plugin.functionaltest

import org.gradle.testkit.runner.GradleRunner
import projektor.server.api.TestRun
import projektor.server.api.coverage.CoverageStats
import retrofit2.Response

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static projektor.plugin.CodeUnderTestWriter.writeFullCoverageSpecFile
import static projektor.plugin.CodeUnderTestWriter.writePartialCoverageSpecFile
import static projektor.plugin.CodeUnderTestWriter.writeSourceCodeFile
import static projektor.plugin.ProjectDirectoryWriter.createSourceDirectory

class MultiProjectCoverageFunctionalSpec extends MultiProjectFunctionalSpecification {
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


    def "should write coverage reports from multiple projects"() {
        given:
        rootBuildFile << """
            projektor {
                serverUrl = '${PROJEKTOR_SERVER_URL}'
                autoPublishOnFailureOnly = false
            }
        """.stripIndent()

        writeSourceCodeFile(sourceDirectory1)
        writeFullCoverageSpecFile(testDirectory1, "FullSpec1")

        writeSourceCodeFile(sourceDirectory2)
        writeFullCoverageSpecFile(testDirectory2, "FullSpec2")

        writeSourceCodeFile(sourceDirectory3)
        writePartialCoverageSpecFile(testDirectory3, "PartialSpec3")

        when:
        def result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test', 'jacocoTestReport', '-i')
                .withPluginClasspath()
                .build()

        String testId = extractTestId(result.output)

        then:
        result.task(":project1:test").outcome == SUCCESS
        result.task(":project1:jacocoTestReport").outcome == SUCCESS

        result.task(":project2:test").outcome == SUCCESS
        result.task(":project2:jacocoTestReport").outcome == SUCCESS

        result.task(":project3:test").outcome == SUCCESS
        result.task(":project3:jacocoTestReport").outcome == SUCCESS

        and:
        Response<TestRun> testRunResponse = projektorTestRunApi.testRun(testId).execute()
        testRunResponse.successful

        and:
        Response<CoverageStats> overallStatsResponse = projektorTestRunApi.coverageOverallStats(testId).execute()
        overallStatsResponse.successful

        CoverageStats overallStats = overallStatsResponse.body()
        overallStats.statementStat.covered == 20
        overallStats.statementStat.missed == 16
        overallStats.statementStat.total == 36
        overallStats.statementStat.coveredPercentage == 55.56

        overallStats.lineStat.covered == 5
        overallStats.lineStat.missed == 1
        overallStats.lineStat.total == 6
        overallStats.lineStat.coveredPercentage == 83.33
    }
}
