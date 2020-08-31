package projektor.plugin.functionaltest

import org.apache.commons.lang3.RandomStringUtils
import org.gradle.testkit.runner.GradleRunner
import projektor.plugin.BuildFileWriter
import projektor.plugin.SpecWriter
import projektor.server.api.PublicId
import retrofit2.Response

class PreviousTestRunFunctionalSpec extends ProjektorPluginFunctionalSpecification {
    File buildFile

    def setup() {
        buildFile = BuildFileWriter.createProjectBuildFile(projectRootDir, true, false)
    }

    def "should public the Git metadata and find the previous test fun"() {
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

        Map<String, String> currentEnv = System.getenv()
        Map<String, String> augmentedEnv = new HashMap<>(currentEnv)
        augmentedEnv.putAll(["GITHUB_REPOSITORY": repoName, "GITHUB_REF": "refs/master/${branchName}".toString()])

        def result1 = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test')
                .withEnvironment(augmentedEnv)
                .withPluginClasspath()
                .buildAndFail()
        String testId1 = extractTestId(result1.output)

        def result2 = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test')
                .withEnvironment(augmentedEnv)
                .withPluginClasspath()
                .buildAndFail()
        String testId2 = extractTestId(result2.output)

        when:
        Response<PublicId> previousTestRunResponse = projektorTestRunApi.previousTestRun(testId1).execute()

        then:
        previousTestRunResponse.successful

        and:
        previousTestRunResponse.body().id == testId2
    }
}
