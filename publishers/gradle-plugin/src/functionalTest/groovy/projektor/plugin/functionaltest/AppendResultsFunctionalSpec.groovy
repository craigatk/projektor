package projektor.plugin.functionaltest

import org.apache.commons.lang3.RandomStringUtils
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import projektor.plugin.BuildFileWriter
import projektor.plugin.SpecWriter
import spock.util.concurrent.PollingConditions

class AppendResultsFunctionalSpec extends ProjektorPluginFunctionalSpecification {
    File buildFile

    def setup() {
        buildFile = BuildFileWriter.createProjectBuildFile(projectRootDir)
    }

    def "should append results from two test runs into one report"() {
        given:
        String repo = "craigatk/${RandomStringUtils.randomAlphabetic(12)}"
        Map<String, String> environment = ["GITHUB_REPOSITORY": repo, "GITHUB_RUN_NUMBER": "42"]

        buildFile << """
            projektor {
                serverUrl = '${PROJEKTOR_SERVER_URL}'
                groupResults = true
            }
        """.stripIndent()

        List<String> expectedTestSuiteClassNames = [
                "FirstSampleSpec",
                "SecondSampleSpec",
                "ThirdSampleSpec"
        ]

        SpecWriter.createTestDirectoryWithFailingTests(projectRootDir, expectedTestSuiteClassNames)

        when:
        BuildResult firstResult = runFailedBuildWithEnvironment(environment, 'test', '--info')

        then:
        firstResult.task(":test").outcome == TaskOutcome.FAILED

        when:
        String firstTestId = extractTestId(firstResult.output)

        then:
        new PollingConditions().eventually {
            assert projektorTestRunApi.testRun(firstTestId).execute().body().summary.totalTestCount == 3
        }

        when:
        BuildResult secondResult = runFailedBuildWithEnvironment(environment, 'test', '--info')

        then:
        secondResult.task(":test").outcome == TaskOutcome.FAILED

        when:
        String secondTestId = extractTestId(secondResult.output)

        then:
        secondTestId == firstTestId

        and:
        new PollingConditions().eventually {
            assert projektorTestRunApi.testRun(secondTestId).execute().body().summary.totalTestCount == 6
        }
    }
}
