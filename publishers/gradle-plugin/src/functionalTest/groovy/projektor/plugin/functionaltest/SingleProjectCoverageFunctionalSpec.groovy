package projektor.plugin.functionaltest

import org.gradle.testkit.runner.TaskOutcome
import projektor.plugin.BuildFileWriter
import projektor.server.api.TestRun
import projektor.server.api.coverage.CoverageStats
import retrofit2.Response

import static projektor.plugin.CodeUnderTestWriter.writeFullCoverageSpecFile
import static projektor.plugin.CodeUnderTestWriter.writeSourceCodeFile
import static projektor.plugin.ProjectDirectoryWriter.createSourceDirectory
import static projektor.plugin.ProjectDirectoryWriter.createTestDirectory

class SingleProjectCoverageFunctionalSpec extends ProjektorPluginFunctionalSpecification {

    File buildFile

    def setup() {
        buildFile = BuildFileWriter.createProjectBuildFile(projectRootDir, true, true)
    }

    def "should upload coverage report from test run"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${PROJEKTOR_SERVER_URL}'
            }
        """.stripIndent()

        File sourceDir = createSourceDirectory(projectRootDir)
        File testDir = createTestDirectory(projectRootDir)

        writeSourceCodeFile(sourceDir)
        writeFullCoverageSpecFile(testDir, "FullSpec")

        when:
        def result = runPassingBuildInCI('test', 'jacocoTestReport', '-i')

        then:
        result.task(":test").outcome == TaskOutcome.SUCCESS
        result.task(":jacocoTestReport").outcome == TaskOutcome.SUCCESS

        when:
        String testId = extractTestId(result.output)

        then:
        Response<TestRun> testRunResponse = projektorTestRunApi.testRun(testId).execute()
        testRunResponse.successful

        and:
        Response<CoverageStats> overallStatsResponse = projektorTestRunApi.coverageOverallStats(testId).execute()
        overallStatsResponse.successful

        CoverageStats overallStats = overallStatsResponse.body()
        overallStats.statementStat.covered == 8
        overallStats.statementStat.missed == 4
        overallStats.statementStat.total == 12
        overallStats.statementStat.coveredPercentage == 66.67

        overallStats.lineStat.covered == 2
        overallStats.lineStat.missed == 0
        overallStats.lineStat.total == 2
        overallStats.lineStat.coveredPercentage == 100.00

        overallStats.branchStat.covered == 0
        overallStats.branchStat.missed == 0
        overallStats.branchStat.total == 0
        overallStats.branchStat.coveredPercentage == 0
    }
}
