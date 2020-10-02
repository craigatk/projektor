package projektor.plugin.functionaltest

import org.apache.commons.lang3.RandomStringUtils
import projektor.plugin.BuildFileWriter
import projektor.plugin.SpecWriter
import projektor.server.api.PublicId
import retrofit2.Response
import spock.util.concurrent.PollingConditions

class PreviousTestRunFunctionalSpec extends ProjektorPluginFunctionalSpecification {
    File buildFile

    def setup() {
        buildFile = BuildFileWriter.createProjectBuildFile(projectRootDir, true, true)
    }

    def "should publish the Git metadata and find the previous test fun with coverage"() {
        given:
        String repoName = "${RandomStringUtils.randomAlphabetic(8)}/${RandomStringUtils.randomAlphabetic(8)}"
        String branchName = "main"

        buildFile << """
            projektor {
                serverUrl = '${PROJEKTOR_SERVER_URL}'
            }
        """.stripIndent()

        File testDirectory = SpecWriter.createTestDirectoryWithPassingTest(projectRootDir, "SampleSpec")

        Map<String, String> augmentedEnv = new HashMap<>()
        augmentedEnv.put("CI", "true")
        augmentedEnv.putAll(["GITHUB_REPOSITORY": repoName, "GITHUB_REF": "refs/master/${branchName}".toString()])

        when:
        def result1 = runPassingBuildWithEnvironment(augmentedEnv, 'test', 'jTR')

        String testId1 = extractTestId(result1.output)

        then:
        new PollingConditions().eventually {
            assert projektorTestRunApi.testRun(testId1).execute().successful
        }

        when:
        SpecWriter.writePassingSpecFile(testDirectory, "SecondSampleSpec")

        def result2 = runPassingBuildWithEnvironment(augmentedEnv, 'test', 'jTR')

        String testId2 = extractTestId(result2.output)

        then:
        new PollingConditions().eventually {
            assert projektorTestRunApi.testRun(testId2).execute().successful
        }

        when:
        Response<PublicId> previousTestRunResponse = projektorTestRunApi.previousTestRun(testId2).execute()

        then:
        previousTestRunResponse.code() == 200

        and:
        previousTestRunResponse.body().id == testId1
    }

    def "when no previous test run should get no-content response"() {
        given:
        String repoName = "${RandomStringUtils.randomAlphabetic(8)}/${RandomStringUtils.randomAlphabetic(8)}"
        String branchName = "main"

        buildFile << """
            projektor {
                serverUrl = '${PROJEKTOR_SERVER_URL}'
            }
        """.stripIndent()

        List<String> expectedTestSuiteClassNames = [
                "FirstSampleSpec",
                "SecondSampleSpec",
                "ThirdSampleSpec"
        ]

        SpecWriter.createTestDirectoryWithFailingTests(projectRootDir, expectedTestSuiteClassNames)

        Map<String, String> augmentedEnv = new HashMap<>()
        augmentedEnv.put("CI", "true")
        augmentedEnv.putAll(["GITHUB_REPOSITORY": repoName, "GITHUB_REF": "refs/master/${branchName}".toString()])

        when:
        def result = runFailedBuildWithEnvironment(augmentedEnv, 'test', 'jTR')

        String testId = extractTestId(result.output)

        then:
        new PollingConditions().eventually {
            assert projektorTestRunApi.testRun(testId).execute().successful
        }

        when:
        Response<PublicId> previousTestRunResponse = projektorTestRunApi.previousTestRun(testId).execute()

        then:
        previousTestRunResponse.code() == 204
    }
}
