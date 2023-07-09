package projektor.plugin.functionaltest

import org.gradle.testkit.runner.TaskOutcome
import projektor.plugin.BuildFileWriter
import projektor.plugin.ProjectBuildFileConfig
import projektor.server.api.TestRun
import projektor.server.api.coverage.CoverageStats
import retrofit2.Response
import spock.util.concurrent.PollingConditions

import static projektor.plugin.CodeUnderTestWriter.writeFullCoverageSpecFile
import static projektor.plugin.CodeUnderTestWriter.writeSourceCodeFile
import static projektor.plugin.ProjectDirectoryWriter.createSourceDirectory
import static projektor.plugin.ProjectDirectoryWriter.createTestDirectory

class SingleProjectKoverCoverageFunctionalSpec extends ProjektorPluginFunctionalSpecification {

    File buildFile

    def setup() {
        buildFile = BuildFileWriter.createProjectBuildFile(projectRootDir, new ProjectBuildFileConfig(includeKoverPlugin: true))
    }

    def "should upload Kover coverage report from test run"() {
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
        def result = runPassingBuildInCI('test', 'koverXmlReport', '-i')

        then:
        result.task(":test").outcome == TaskOutcome.SUCCESS
        result.task(":koverXmlReport").outcome == TaskOutcome.SUCCESS

        when:
        String testId = extractTestId(result.output)

        then:
        new PollingConditions().eventually {
            Response<TestRun> testRunResponse = projektorTestRunApi.testRun(testId).execute()
            assert testRunResponse.successful
        }

        and:
        new PollingConditions().eventually {
            Response<CoverageStats> overallStatsResponse = projektorTestRunApi.coverageOverallStats(testId).execute()
            assert overallStatsResponse.successful

            CoverageStats overallStats = overallStatsResponse.body()
            assert overallStats.statementStat.covered == 8
            assert overallStats.statementStat.missed == 4
            assert overallStats.statementStat.total == 12
            assert overallStats.statementStat.coveredPercentage == 66.67

            assert overallStats.lineStat.covered == 2
            assert overallStats.lineStat.missed == 0
            assert overallStats.lineStat.total == 2
            assert overallStats.lineStat.coveredPercentage == 100.00

            assert overallStats.branchStat.covered == 0
            assert overallStats.branchStat.missed == 0
            assert overallStats.branchStat.total == 0
            assert overallStats.branchStat.coveredPercentage == 0
        }
    }
}
