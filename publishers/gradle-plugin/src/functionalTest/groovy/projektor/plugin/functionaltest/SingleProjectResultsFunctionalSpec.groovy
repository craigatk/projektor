package projektor.plugin.functionaltest

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import projektor.plugin.BuildFileWriter
import projektor.plugin.SpecWriter
import projektor.server.api.TestRun
import projektor.server.api.TestSuite
import retrofit2.Response

class SingleProjectResultsFunctionalSpec extends ProjektorPluginFunctionalSpecification {
    File buildFile

    def setup() {
        buildFile = BuildFileWriter.createProjectBuildFile(projectRootDir)
    }

    def "should send results from single project to server"() {
        given:
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

        when:
        def result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test')
                .withPluginClasspath()
                .buildAndFail()

        then:
        result.task(":test").outcome == TaskOutcome.FAILED

        when:
        String testId = extractTestId(result.output)

        Response<TestRun> testRunResponse = projektorTestRunApi.testRun(testId).execute()

        then:
        testRunResponse.successful

        TestRun testRun = testRunResponse.body()
        testRun.testSuites.size() == expectedTestSuiteClassNames.size()

        expectedTestSuiteClassNames.each { expectedClassName ->
            assert testRun.testSuites.find { it.className == expectedClassName }
        }

        and:
        testRun.summary.wallClockDuration != null
        testRun.summary.wallClockDuration > BigDecimal.ZERO

        when:
        Response<List<TestSuite>> testSuitesResponse = projektorTestRunApi.testSuites(testId).execute()

        then:
        testSuitesResponse.successful

        List<TestSuite> testSuites = testSuitesResponse.body()
        testSuites.size() == expectedTestSuiteClassNames.size()

        expectedTestSuiteClassNames.each { expectedClassName ->
            assert testSuites.find { it.className == expectedClassName }
        }
    }

}
