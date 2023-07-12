package projektor.plugin.functionaltest

import org.gradle.testkit.runner.TaskOutcome
import projektor.plugin.BuildFileWriter
import projektor.plugin.ProjectBuildFileConfig
import projektor.server.api.TestRun
import projektor.server.api.coverage.CoverageStats
import retrofit2.Response
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

import static projektor.plugin.CodeUnderTestWriter.writeFullCoverageKotestFile
import static projektor.plugin.CodeUnderTestWriter.writeKotlinSourceCodeFile
import static projektor.plugin.ProjectDirectoryWriter.createKotlinSourceDirectory
import static projektor.plugin.ProjectDirectoryWriter.createKotlinTestDirectory

class SingleProjectKoverCoverageFunctionalSpec extends ProjektorPluginFunctionalSpecification {
    @Unroll
    def "should upload Kover coverage report from test run with kover version #koverVersion"() {
        given:
        File buildFile = BuildFileWriter.createProjectBuildFile(projectRootDir, new ProjectBuildFileConfig(includeKoverPlugin: true, koverPluginVersion: koverVersion))

        buildFile << """
            projektor {
                serverUrl = '${PROJEKTOR_SERVER_URL}'
            }
        """.stripIndent()

        File sourceDir = createKotlinSourceDirectory(projectRootDir)
        File testDir = createKotlinTestDirectory(projectRootDir)

        writeKotlinSourceCodeFile(sourceDir)
        writeFullCoverageKotestFile(testDir, "FooTest")

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
            assert overallStats.statementStat.covered == 4
            assert overallStats.statementStat.missed == 0
            assert overallStats.statementStat.total == 4
            assert overallStats.statementStat.coveredPercentage == 100.00

            assert overallStats.lineStat.covered == 2
            assert overallStats.lineStat.missed == 0
            assert overallStats.lineStat.total == 2
            assert overallStats.lineStat.coveredPercentage == 100.00

            assert overallStats.branchStat.covered == 0
            assert overallStats.branchStat.missed == 0
            assert overallStats.branchStat.total == 0
            assert overallStats.branchStat.coveredPercentage == 0
        }

        where:
        koverVersion || _
        "0.4.4"      || _
        "0.7.2"      || _
    }
}
