package projektor.plugin.functionaltest

import org.gradle.testkit.runner.TaskOutcome
import projektor.plugin.BuildFileWriter
import projektor.plugin.SpecFileConfig
import projektor.server.api.TestCase
import projektor.server.api.TestOutput
import retrofit2.Response
import spock.util.concurrent.PollingConditions

import static projektor.plugin.ProjectDirectoryWriter.createTestDirectory
import static projektor.plugin.SpecWriter.writeSpecFile

class TestCaseSystemOutFunctionalSpec extends ProjektorPluginFunctionalSpecification {
    File buildFile

    def setup() {
        buildFile = BuildFileWriter.createProjectBuildFile(projectRootDir)
    }

    def "should send system out at the test case level"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${PROJEKTOR_SERVER_URL}'
            }
            
            test {
                reports {
                    junitXml {
                        outputPerTestCase = true
                    }
                }
            }
        """.stripIndent()

        File testDirectory = createTestDirectory(projectRootDir)
        SpecFileConfig specFileConfig = new SpecFileConfig(
                passing: false,
                additionalCodeLines: ["println 'Here is some system out'"]
        )
        writeSpecFile(testDirectory, "FailingWithOutput", specFileConfig)

        when:
        def result = runFailedLocalBuild('test')

        then:
        result.task(":test").outcome == TaskOutcome.FAILED

        when:
        String testId = extractTestId(result.output)

        then:
        new PollingConditions().eventually {
            assert projektorTestRunApi.testRun(testId).execute().successful
        }

        Response<TestOutput> testOutputResponse = projektorTestRunApi.testCaseSystemOut(testId, 1, 1).execute()
        testOutputResponse.successful

        testOutputResponse.body().value.contains("Here is some system out")

        and:
        Response<TestCase> testCaseResponse = projektorTestRunApi.testCase(testId, 1, 1).execute()
        testCaseResponse.successful

        TestCase testCase = testCaseResponse.body()
        testCase.hasSystemOut
        testCase.hasSystemOutTestCase
        !testCase.hasSystemOutTestSuite
    }
}
