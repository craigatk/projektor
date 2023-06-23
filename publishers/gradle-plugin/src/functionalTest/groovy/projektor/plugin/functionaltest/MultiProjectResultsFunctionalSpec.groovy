package projektor.plugin.functionaltest

import projektor.plugin.SpecFileConfig
import projektor.plugin.SpecWriter
import projektor.server.api.TestOutput
import projektor.server.api.TestRun
import projektor.server.api.TestSuite
import retrofit2.Response

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class MultiProjectResultsFunctionalSpec extends MultiProjectFunctionalSpecification {
    def setup() {
        SpecWriter.writeFailingSpecFiles(testDirectory1, ["FailingSpec1", "FailingSpec2"])

        SpecWriter.writeSpecFile(
                testDirectory2,
                "FailingSpecWithOutput",
                new SpecFileConfig(passing: false, additionalCodeLines: (1..100).collect { "println 'Output line ${it}'"})
        )

        SpecWriter.writePassingSpecFiles(testDirectory3, ["PassingSpec1", "PassingSpec2"])
    }

    def "should send results from multiple subprojects to server"() {
        given:
        rootBuildFile << """
            projektor {
                serverUrl = '${PROJEKTOR_SERVER_URL}'
            }
        """.stripIndent()

        when:
        def result = runFailedLocalBuild('test', '--continue')

        println result.output

        then:
        result.task(":project1:test").outcome == FAILED
        result.task(":project2:test").outcome == FAILED
        result.task(":project3:test").outcome == SUCCESS

        and:
        String testId = extractTestId(result.output)
        waitForTestRunProcessingToComplete(testId)

        Response<TestRun> testRunResponse = projektorTestRunApi.testRun(testId).execute()

        TestRun testRun = testRunResponse.body()
        !testRun.summary.passed
        testRun.summary.totalFailureCount == 3
        testRun.summary.totalPassingCount == 2
        testRun.summary.totalTestCount == 5

        and:
        TestSuite failingSpecWithOutputTestSuite = testRun.testSuites.find { it.className == "FailingSpecWithOutput" }
        failingSpecWithOutputTestSuite.hasSystemOut == true

        and:
        Response<TestOutput> failingSpecOutputResponse = projektorTestRunApi.testSuiteSystemOut(testId, failingSpecWithOutputTestSuite.idx).execute()
        failingSpecOutputResponse.successful

        TestOutput failingSpecOutput = failingSpecOutputResponse.body()
        List<String> outputLines = failingSpecOutput.value.readLines()
        outputLines.size() == 100
        outputLines.contains("Output line 1")
        outputLines.contains("Output line 100")
    }
}
