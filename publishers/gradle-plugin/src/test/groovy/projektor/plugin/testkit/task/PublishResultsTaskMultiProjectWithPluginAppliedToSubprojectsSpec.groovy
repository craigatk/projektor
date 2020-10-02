package projektor.plugin.testkit.task

import com.github.tomakehurst.wiremock.verification.LoggedRequest
import projektor.plugin.SpecWriter
import projektor.plugin.testkit.MultiProjectWithPluginAppliedToSubprojectsSpec

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static projektor.plugin.PluginOutput.verifyOutputContainsReportLink

class PublishResultsTaskMultiProjectWithPluginAppliedToSubprojectsSpec extends MultiProjectWithPluginAppliedToSubprojectsSpec {

    def setup() {
        SpecWriter.writePassingSpecFiles(testDirectory1, ["Sample1Spec1", "Sample1Spec2"])
        SpecWriter.writePassingSpecFiles(testDirectory2, ["Sample2Spec1", "Sample2Spec2"])
        SpecWriter.writePassingSpecFiles(testDirectory3, ["Sample3Spec1", "Sample3Spec2"])

        rootBuildFile << """
            plugins {
                id 'dev.projektor.publish'
            }

            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()
    }

    def "when running publish task from root project should send results from each subproject"() {
        given:
        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def testResult = runSuccessfulLocalBuild('test')

        then:
        testResult.task(":project1:test").outcome == SUCCESS
        testResult.task(":project2:test").outcome == SUCCESS
        testResult.task(":project3:test").outcome == SUCCESS

        and:
        resultsStubber.findResultsRequests().size() == 0

        when:
        def publishResults = runSuccessfulLocalBuild('publishResults')

        then:
        publishResults.task(":project1:publishResults").outcome == SUCCESS
        publishResults.task(":project2:publishResults").outcome == SUCCESS
        publishResults.task(":project3:publishResults").outcome == SUCCESS

        and:
        verifyOutputContainsReportLink(publishResults.output, serverUrl, resultsId)

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
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
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def testResult = runSuccessfulLocalBuild('test')

        then:
        testResult.task(":project1:test").outcome == SUCCESS
        testResult.task(":project2:test").outcome == SUCCESS
        testResult.task(":project3:test").outcome == SUCCESS

        and:
        resultsStubber.findResultsRequests().size() == 0

        when:
        def publishResults = runSuccessfulLocalBuild(':project1:publishResults')

        then:
        publishResults.task(":project1:publishResults").outcome == SUCCESS

        and:
        verifyOutputContainsReportLink(publishResults.output, serverUrl, resultsId)

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
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
