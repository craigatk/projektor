package projektor.plugin.functionaltest

import org.apache.commons.lang3.RandomStringUtils
import projektor.server.api.TestRun
import projektor.server.api.coverage.Coverage
import projektor.server.api.coverage.CoverageStats
import retrofit2.Response
import spock.util.concurrent.PollingConditions

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
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
            }
        """.stripIndent()

        writeSourceCodeFile(sourceDirectory1)
        writeFullCoverageSpecFile(testDirectory1, "FullSpec1")

        writeSourceCodeFile(sourceDirectory2)
        writeFullCoverageSpecFile(testDirectory2, "FullSpec2")

        writeSourceCodeFile(sourceDirectory3)
        writePartialCoverageSpecFile(testDirectory3, "PartialSpec3")

        when:
        def result = runPassingBuildInCI('test', 'jacocoTestReport', '-i')

        String testId = extractTestId(result.output)

        then:
        result.task(":project1:test").outcome == SUCCESS
        result.task(":project1:jacocoTestReport").outcome == SUCCESS

        result.task(":project2:test").outcome == SUCCESS
        result.task(":project2:jacocoTestReport").outcome == SUCCESS

        result.task(":project3:test").outcome == SUCCESS
        result.task(":project3:jacocoTestReport").outcome == SUCCESS

        and:
        new PollingConditions().eventually {
            Response<TestRun> testRunResponse = projektorTestRunApi.testRun(testId).execute()
            assert testRunResponse.successful
        }

        and:
        new PollingConditions().eventually {
            Response<CoverageStats> overallStatsResponse = projektorTestRunApi.coverageOverallStats(testId).execute()
            assert overallStatsResponse.successful

            CoverageStats overallStats = overallStatsResponse.body()
            assert overallStats.statementStat.covered == 20
            assert overallStats.statementStat.missed == 16
            assert overallStats.statementStat.total == 36
            assert overallStats.statementStat.coveredPercentage == 55.56

            assert overallStats.lineStat.covered == 5
            assert overallStats.lineStat.missed == 1
            assert overallStats.lineStat.total == 6
            assert overallStats.lineStat.coveredPercentage == 83.33
        }
    }

    def "when tests not executed but code coverage exists and running in CI should publish coverage reports from multiple projects"() {
        given:
        rootBuildFile << """
            projektor {
                serverUrl = '${PROJEKTOR_SERVER_URL}'
            }
        """.stripIndent()

        writeSourceCodeFile(sourceDirectory1)
        writeFullCoverageSpecFile(testDirectory1, "FullSpec1")

        writeSourceCodeFile(sourceDirectory2)
        writeFullCoverageSpecFile(testDirectory2, "FullSpec2")

        writeSourceCodeFile(sourceDirectory3)
        writePartialCoverageSpecFile(testDirectory3, "PartialSpec3")

        String repoName = "${RandomStringUtils.randomAlphabetic(8)}/${RandomStringUtils.randomAlphabetic(8)}"
        String branchName = "main"

        Map<String, String> augmentedEnv = new HashMap<>()
        augmentedEnv.putAll(["CI": "true"])
        augmentedEnv.putAll(["GITHUB_REPOSITORY": repoName, "GITHUB_REF": "refs/master/${branchName}".toString()])

        when:
        def result1 = runPassingBuildWithEnvironment(augmentedEnv, 'test', 'jacocoTestReport')

        String testId1 = extractTestId(result1.output)

        then:
        result1.task(":project1:test").outcome == SUCCESS
        result1.task(":project1:jacocoTestReport").outcome == SUCCESS

        result1.task(":project2:test").outcome == SUCCESS
        result1.task(":project2:jacocoTestReport").outcome == SUCCESS

        result1.task(":project3:test").outcome == SUCCESS
        result1.task(":project3:jacocoTestReport").outcome == SUCCESS

        and:
        new PollingConditions().eventually {
            Response<TestRun> testRunResponse1 = projektorTestRunApi.testRun(testId1).execute()
            assert testRunResponse1.successful
        }

        and:
        new PollingConditions().eventually {
            Response<CoverageStats> overallStatsResponse1 = projektorTestRunApi.coverageOverallStats(testId1).execute()
            assert overallStatsResponse1.successful

            CoverageStats overallStats = overallStatsResponse1.body()
            assert overallStats.statementStat.covered == 20
            assert overallStats.statementStat.missed == 16
            assert overallStats.statementStat.total == 36
            assert overallStats.statementStat.coveredPercentage == 55.56

            assert overallStats.lineStat.covered == 5
            assert overallStats.lineStat.missed == 1
            assert overallStats.lineStat.total == 6
            assert overallStats.lineStat.coveredPercentage == 83.33
        }

        when:
        def result2 = runPassingBuildWithEnvironment(augmentedEnv, 'test', 'jacocoTestReport')

        String testId2 = extractTestId(result2.output)

        then:
        result2.task(":project1:test").outcome == UP_TO_DATE
        result2.task(":project1:jacocoTestReport").outcome == UP_TO_DATE

        result2.task(":project2:test").outcome == UP_TO_DATE
        result2.task(":project2:jacocoTestReport").outcome == UP_TO_DATE

        result2.task(":project3:test").outcome == UP_TO_DATE
        result2.task(":project3:jacocoTestReport").outcome == UP_TO_DATE

        and:
        new PollingConditions().eventually {
            Response<TestRun> testRunResponse2 = projektorTestRunApi.testRun(testId2).execute()
            assert testRunResponse2.successful
        }

        and:
        new PollingConditions().eventually {
            Response<Coverage> coverageResponse2 = projektorTestRunApi.coverage(testId2).execute()
            assert coverageResponse2.successful

            Coverage coverage2 = coverageResponse2.body()
            assert coverage2.previousTestRunId == testId1

            assert coverage2.overallStats.statementStat.coveredPercentage == 55.56
            assert coverage2.overallStats.statementStat.coveredPercentageDelta == 0.00

            assert coverage2.overallStats.lineStat.coveredPercentage == 83.33
            assert coverage2.overallStats.lineStat.coveredPercentageDelta == 0.00
        }
    }
}
