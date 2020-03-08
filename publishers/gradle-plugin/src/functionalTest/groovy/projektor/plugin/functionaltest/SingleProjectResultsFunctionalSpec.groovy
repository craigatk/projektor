package projektor.plugin.functionaltest

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import projektor.server.api.TestRun
import retrofit2.Response

class SingleProjectResultsFunctionalSpec extends SingleProjectFunctionalSpecification {

    def "should send results from single project to server"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${PROJEKTOR_SERVER_URL}'
            }
        """.stripIndent()

        File testDirectory = specWriter.createTestDirectory(projectRootDir)
        specWriter.writeSpecFile(testDirectory, "SampleSpec")

        when:
        def result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test')
                .withPluginClasspath()
                .build()

        then:
        result.task(":test").outcome == TaskOutcome.SUCCESS

        when:
        String testId = extractTestId(result.output)

        Response<TestRun> testRunResponse = projektorResultsApi.testRun(testId).execute()

        then:
        testRunResponse.successful

        and:
        TestRun testRun = testRunResponse.body()
        testRun.testSuites.size() == 1
    }

}
