package projektor.plugin.testkit

import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.gradle.testkit.runner.GradleRunner

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class PluginAutoPublishMultiProjectWithPluginAppliedToSubprojectsSpec extends MultiProjectWithPluginAppliedToSubprojectsSpec {

    def setup() {
        specWriter.writeSpecFile(testDirectory1, "Sample1Spec1")
        specWriter.writeSpecFile(testDirectory1, "Sample1Spec2")

        specWriter.writeSpecFile(testDirectory2, "Sample2Spec1")
        specWriter.writeSpecFile(testDirectory2, "Sample2Spec2")

        specWriter.writeSpecFile(testDirectory3, "Sample3Spec1")
        specWriter.writeSpecFile(testDirectory3, "Sample3Spec2")
    }

    def "should send results from multiple subprojects in one blob to server"() {
        given:
        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test')
                .withPluginClasspath()
                .build()

        then:
        println result.output

        result.task(":project1:test").outcome == SUCCESS
        result.task(":project2:test").outcome == SUCCESS
        result.task(":project3:test").outcome == SUCCESS

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1

        and:
        String resultsBlob = resultsRequests[0].bodyAsString

        resultsBlob.contains("Sample1Spec1")
        resultsBlob.contains("Sample1Spec2")
        resultsBlob.contains("Sample2Spec1")
        resultsBlob.contains("Sample2Spec1")
        resultsBlob.contains("Sample3Spec1")
        resultsBlob.contains("Sample3Spec2")
    }

    def "when plugin applied to parent project and all subprojects should send results from multiple subprojects in one blob to server"() {
        given:
        rootBuildFile << """
            plugins {
                id 'dev.projektor.publish'
            }

            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        String resultsId = "FEE999"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test')
                .withPluginClasspath()
                .build()

        then:
        println result.output

        result.task(":project1:test").outcome == SUCCESS
        result.task(":project2:test").outcome == SUCCESS
        result.task(":project3:test").outcome == SUCCESS

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1

        and:
        String resultsBlob = resultsRequests[0].bodyAsString

        resultsBlob.contains("Sample1Spec1")
        resultsBlob.contains("Sample1Spec2")
        resultsBlob.contains("Sample2Spec1")
        resultsBlob.contains("Sample2Spec1")
        resultsBlob.contains("Sample3Spec1")
        resultsBlob.contains("Sample3Spec2")
    }

    def "should send results when executing just one subproject build"() {
        given:
        String resultsId = "DEF345"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('-p', 'project1', 'test')
                .withPluginClasspath()
                .build()

        then:
        println result.output

        result.task(":project1:test").outcome == SUCCESS
        !result.task(":project2:test")
        !result.task(":project3:test")

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1

        and:
        String resultsBlob = resultsRequests[0].bodyAsString

        resultsBlob.contains("Sample1Spec1")
        resultsBlob.contains("Sample1Spec2")
        !resultsBlob.contains("Sample2Spec1")
        !resultsBlob.contains("Sample2Spec1")
        !resultsBlob.contains("Sample3Spec1")
        !resultsBlob.contains("Sample3Spec2")
    }
}
