package projektor.plugin.functionaltest

import org.gradle.testkit.runner.TaskOutcome
import projektor.plugin.BuildFileWriter
import projektor.plugin.SpecWriter
import projektor.server.api.metadata.TestRunMetadata
import retrofit2.Response

class MetadataFunctionalSpec extends ProjektorPluginFunctionalSpecification {
    File buildFile

    def setup() {
        buildFile = BuildFileWriter.createProjectBuildFile(projectRootDir)
    }

    def "should send results metadata to server"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${PROJEKTOR_SERVER_URL}'
            }
        """.stripIndent()

        List<String> expectedTestSuiteClassNames = [
                "FirstSampleSpec",
        ]

        SpecWriter.createTestDirectoryWithFailingTests(projectRootDir, expectedTestSuiteClassNames)

        when:
        def result = runFailedBuildWithEnvironment([CI: "true"], "test")

        then:
        result.task(":test").outcome == TaskOutcome.FAILED

        when:
        String testId = extractTestId(result.output)

        Response<TestRunMetadata> metadataResponse = projektorTestRunMetadataApi.testRunMetadata(testId).execute()

        then:
        metadataResponse.successful

        TestRunMetadata testRunMetadata = metadataResponse.body()
        testRunMetadata.ci == true
    }
}
