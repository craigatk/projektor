package projektor.plugin.testkit

import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.gradle.testkit.runner.GradleRunner
import projektor.plugin.SpecWriter

import static org.gradle.testkit.runner.TaskOutcome.FAILED

class PluginAutoPublishMultiProjectSpec extends MultiProjectSpec {

    def setup() {
        SpecWriter.writeFailingSpecFile(testDirectory1, "Sample1Spec1")
        SpecWriter.writeFailingSpecFile(testDirectory1, "Sample1Spec2")

        SpecWriter.writeFailingSpecFile(testDirectory2, "Sample2Spec1")
        SpecWriter.writeFailingSpecFile(testDirectory2, "Sample2Spec2")

        SpecWriter.writeFailingSpecFile(testDirectory3, "Sample3Spec1")
        SpecWriter.writeFailingSpecFile(testDirectory3, "Sample3Spec2")
    }

    def "should send results from multiple subprojects in one blob to server"() {
        given:
        rootBuildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test', '--continue')
                .withPluginClasspath()
                .buildAndFail()

        then:
        result.task(":project1:test").outcome == FAILED
        result.task(":project2:test").outcome == FAILED
        result.task(":project3:test").outcome == FAILED

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

    def "when running test tasks in a subset of the subprojects should only send results from those subprojects"() {
        given:
        rootBuildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def firstResult = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments(':project1:test')
                .withPluginClasspath()
                .buildAndFail()

        then:
        firstResult.task(":project1:test").outcome == FAILED

        and:
        List<LoggedRequest> firstResultsRequests = resultsStubber.findResultsRequests()
        firstResultsRequests.size() == 1

        and:
        String firstResultsBlob = firstResultsRequests[0].bodyAsString

        firstResultsBlob.contains("Sample1Spec1")
        firstResultsBlob.contains("Sample1Spec2")
        !firstResultsBlob.contains("Sample2Spec1")
        !firstResultsBlob.contains("Sample2Spec1")
        !firstResultsBlob.contains("Sample3Spec1")
        !firstResultsBlob.contains("Sample3Spec2")

        when:
        wireMockRule.resetRequests()

        def secondResult = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments(':project2:test', ':project3:test', '--continue')
                .withPluginClasspath()
                .buildAndFail()

        then:
        secondResult.task(":project2:test").outcome == FAILED
        secondResult.task(":project3:test").outcome == FAILED

        and:
        List<LoggedRequest> secondResultsRequests = resultsStubber.findResultsRequests()
        secondResultsRequests.size() == 1

        and:
        String secondResultsBlob = secondResultsRequests[0].bodyAsString

        !secondResultsBlob.contains("Sample1Spec1")
        !secondResultsBlob.contains("Sample1Spec2")
        secondResultsBlob.contains("Sample2Spec1")
        secondResultsBlob.contains("Sample2Spec1")
        secondResultsBlob.contains("Sample3Spec1")
        secondResultsBlob.contains("Sample3Spec2")
    }
}
