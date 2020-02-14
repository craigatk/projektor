package projektor.plugin.testkit.task

import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.gradle.testkit.runner.GradleRunner
import projektor.plugin.testkit.MultiProjectWithPluginAppliedToSubprojectsSpec

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class PublishResultsTaskMultiProjectWithPluginAppliedToSubprojectsSpec extends MultiProjectWithPluginAppliedToSubprojectsSpec {

    def setup() {
        specWriter.writeSpecFile(testDirectory1, "Sample1Spec1")
        specWriter.writeSpecFile(testDirectory1, "Sample1Spec2")

        specWriter.writeSpecFile(testDirectory2, "Sample2Spec1")
        specWriter.writeSpecFile(testDirectory2, "Sample2Spec2")

        specWriter.writeSpecFile(testDirectory3, "Sample3Spec1")
        specWriter.writeSpecFile(testDirectory3, "Sample3Spec2")

        rootBuildFile << """
            plugins {
                id 'dev.projektor.publish'
            }

            projektor {
                serverUrl = '${serverUrl}'
                autoPublish = false
            }
        """.stripIndent()
    }

    @Override
    String getAdditionalPluginConfig() {
        "autoPublish = false"
    }

    def "when running publish task from root project should send results from each subproject"() {
        given:
        String resultsId = "ABC123"
        wireMockStubber.stubResultsPostSuccess(resultsId)

        when:
        def testResult = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test')
                .withPluginClasspath()
                .build()

        then:
        testResult.task(":project1:test").outcome == SUCCESS
        testResult.task(":project2:test").outcome == SUCCESS
        testResult.task(":project3:test").outcome == SUCCESS

        and:
        wireMockStubber.findResultsRequests().size() == 0

        when:
        def publishResults = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('publishResults')
                .withPluginClasspath()
                .build()

        then:
        publishResults.task(":project1:publishResults").outcome == SUCCESS
        publishResults.task(":project2:publishResults").outcome == SUCCESS
        publishResults.task(":project3:publishResults").outcome == SUCCESS

        and:
        publishResults.output.contains("View Projektor report at: ${serverUrl}/tests/${resultsId}")

        and:
        List<LoggedRequest> resultsRequests = wireMockStubber.findResultsRequests()
        resultsRequests.size() == 3

        and:
        String resultsBlob1 = resultsRequests.find { it.bodyAsString.contains("Sample1Spec1") }
        resultsBlob1.contains("Sample1Spec2")

        String resultsBlob2 = resultsRequests.find { it.bodyAsString.contains("Sample2Spec1") }
        resultsBlob2.contains("Sample2Spec2")

        String resultsBlob3 = resultsRequests.find { it.bodyAsString.contains("Sample3Spec1") }
        resultsBlob3.contains("Sample3Spec2")
    }

    def "when running publish task from single project should send results from only that subproject"() {
        given:
        String resultsId = "ABC123"
        wireMockStubber.stubResultsPostSuccess(resultsId)

        when:
        def testResult = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test')
                .withPluginClasspath()
                .build()

        then:
        testResult.task(":project1:test").outcome == SUCCESS
        testResult.task(":project2:test").outcome == SUCCESS
        testResult.task(":project3:test").outcome == SUCCESS

        and:
        wireMockStubber.findResultsRequests().size() == 0

        when:
        def publishResults = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments(':project1:publishResults')
                .withPluginClasspath()
                .build()

        then:
        publishResults.task(":project1:publishResults").outcome == SUCCESS

        and:
        publishResults.output.contains("View Projektor report at: ${serverUrl}/tests/${resultsId}")

        and:
        List<LoggedRequest> resultsRequests = wireMockStubber.findResultsRequests()
        resultsRequests.size() == 1

        and:
        String resultsBlob1 = resultsRequests.find { it.bodyAsString.contains("Sample1Spec1") }
        resultsBlob1.contains("Sample1Spec2")

        !resultsBlob1.contains("Sample2Spec1")
        !resultsBlob1.contains("Sample2Spec2")
        !resultsBlob1.contains("Sample3Spec1")
        !resultsBlob1.contains("Sample3Spec2")
    }
}
