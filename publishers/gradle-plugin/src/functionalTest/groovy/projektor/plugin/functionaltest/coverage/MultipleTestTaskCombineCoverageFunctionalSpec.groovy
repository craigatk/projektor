package projektor.plugin.functionaltest.coverage

import org.gradle.testkit.runner.TaskOutcome
import projektor.plugin.BuildFileWriter
import projektor.plugin.MultiTestTaskCoverageWriter
import projektor.plugin.functionaltest.ProjektorPluginFunctionalSpecification
import projektor.server.api.TestRun
import projektor.server.api.coverage.CoverageStats
import retrofit2.Response
import spock.util.concurrent.PollingConditions

import static projektor.plugin.ProjectDirectoryWriter.createIntegrationTestDirectory
import static projektor.plugin.ProjectDirectoryWriter.createSourceDirectory
import static projektor.plugin.ProjectDirectoryWriter.createTestDirectory

class MultipleTestTaskCombineCoverageFunctionalSpec extends ProjektorPluginFunctionalSpecification {

    File buildFile

    def setup() {
        buildFile = BuildFileWriter.createProjectBuildFile(projectRootDir, true, true)
    }

    def "should upload coverage reports from multiple test tasks and combine the coverage data on the server"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${PROJEKTOR_SERVER_URL}'
            }
            
            sourceSets {
                intTest {
                    compileClasspath += sourceSets.main.output
                    runtimeClasspath += sourceSets.main.output
                }
            }
            
            configurations {
                intTestImplementation.extendsFrom testImplementation
                intTestRuntimeOnly.extendsFrom runtimeOnly
            }

            task integrationTest(type: Test) {
                description = 'Runs integration tests.'
                group = 'verification'
            
                testClassesDirs = sourceSets.intTest.output.classesDirs
                classpath = sourceSets.intTest.runtimeClasspath
            }
            
            task integrationTestJacocoReport(type:JacocoReport) {
                dependsOn integrationTest
                executionData integrationTest
                sourceSets sourceSets.main
            }
            
            jacocoTestReport.dependsOn(test)
        """.stripIndent()

        File sourceDir = createSourceDirectory(projectRootDir)
        File testDir = createTestDirectory(projectRootDir)
        File integrationTestDir = createIntegrationTestDirectory(projectRootDir)

        MultiTestTaskCoverageWriter.writeSourceCodeFile(sourceDir)
        MultiTestTaskCoverageWriter.writeFirstPartialCoverageSpecFile(testDir, "ExampleUnitSpec")
        MultiTestTaskCoverageWriter.writeSecondPartialCoverageSpecFile(integrationTestDir, "ExampleIntegrationSpec")


        when:
        def result = runPassingBuildInCI('test', 'integrationTest', 'jacocoTestReport', 'integrationTestJacocoReport', '-i', '--stacktrace')

        then:
        result.task(":test")?.outcome == TaskOutcome.SUCCESS
        result.task(":integrationTest").outcome == TaskOutcome.SUCCESS
        result.task(":jacocoTestReport")?.outcome == TaskOutcome.SUCCESS
        result.task(":integrationTestJacocoReport")?.outcome == TaskOutcome.SUCCESS

        when:
        String testId = extractTestId(result.output)

        then:
        new PollingConditions().eventually {
            Response<TestRun> testRunResponse = projektorTestRunApi.testRun(testId).execute()
            assert testRunResponse.successful
        }

        new PollingConditions().eventually {
            Response<CoverageStats> overallStatsResponse = projektorTestRunApi.coverageOverallStats(testId).execute()
            assert overallStatsResponse.successful

            CoverageStats overallStats = overallStatsResponse.body()
            assert overallStats.lineStat.covered == 7
            assert overallStats.lineStat.missed == 2
            assert overallStats.lineStat.total == 9
            assert overallStats.lineStat.coveredPercentage == 77.78

            assert overallStats.branchStat.covered == 4
            assert overallStats.branchStat.missed == 2
            assert overallStats.branchStat.total == 6
            assert overallStats.branchStat.coveredPercentage == 66.67

            assert overallStats.statementStat.covered == 20
            assert overallStats.statementStat.missed == 10
            assert overallStats.statementStat.total == 30
            assert overallStats.statementStat.coveredPercentage == 66.67
        }
    }
}
