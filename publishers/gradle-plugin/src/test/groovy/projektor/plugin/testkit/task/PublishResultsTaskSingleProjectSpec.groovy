package projektor.plugin.testkit.task

import com.github.tomakehurst.wiremock.verification.LoggedRequest
import projektor.plugin.SpecWriter
import projektor.plugin.results.grouped.GitMetadata
import projektor.plugin.results.grouped.GroupedResults
import projektor.plugin.testkit.SingleProjectSpec

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static projektor.plugin.PluginOutput.verifyOutputContainsReportLink

class PublishResultsTaskSingleProjectSpec extends SingleProjectSpec {

    def "should publish results with publish task"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithPassingTest(projectRootDir, "SampleSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def testResult = runSuccessfulLocalBuild('test')

        then:
        testResult.task(":test").outcome == SUCCESS

        and:
        resultsStubber.findResultsRequests().size() == 0

        when:
        def publishResults = runSuccessfulLocalBuild('publishResults')

        then:
        publishResults.task(":publishResults").outcome == SUCCESS

        and:
        verifyOutputContainsReportLink(publishResults.output, serverUrl, resultsId)

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1

        and:
        String requestBody = resultsRequests[0].bodyAsString

        requestBody.contains('SampleSpec')
        requestBody.contains('sample test')
    }

    def "should publish results with Git metadata"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithPassingTest(projectRootDir, "SampleSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def testResult = runSuccessfulLocalBuild('test')

        then:
        testResult.task(":test").outcome == SUCCESS

        and:
        resultsStubber.findResultsRequests().size() == 0

        when:
        def publishResults = runSuccessfulBuildWithEnvironment(
                ['GITHUB_REPOSITORY': 'projektor/projektor', "GITHUB_REF": "refs/head/main", "GITHUB_SHA": "ffac537e6cbbf934b08745a378932722df287a53"],
                'publishResults'
        )

        then:
        publishResults.task(":publishResults").outcome == SUCCESS

        and:
        verifyOutputContainsReportLink(publishResults.output, serverUrl, resultsId)

        and:
        List<GroupedResults> resultsRequests = resultsStubber.findResultsRequestBodies()
        resultsRequests.size() == 1

        GitMetadata gitMetadata = resultsRequests[0].metadata?.git
        gitMetadata != null

        gitMetadata.repoName == "projektor/projektor"
        gitMetadata.branchName == "main"
        gitMetadata.isMainBranch
        gitMetadata.commitSha == "ffac537e6cbbf934b08745a378932722df287a53"
    }

    def "should publish results with Git metadata and project name"() {
        given:
        String projectName = "my-project"

        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
            
            task projektorPublish(type: projektor.plugin.ProjektorManualPublishTask) {
                serverUrl = "${serverUrl}"
                additionalResultsDirs = ["build/test-results"]
                projectName = "${projectName}"
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithPassingTest(projectRootDir, "SampleSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def testResult = runSuccessfulLocalBuild('test')

        then:
        testResult.task(":test").outcome == SUCCESS

        and:
        resultsStubber.findResultsRequests().size() == 0

        when:
        def publishResults = runSuccessfulBuildWithEnvironment(
                ['GITHUB_REPOSITORY': 'projektor/projektor', "GITHUB_REF": "refs/head/main", "GITHUB_SHA": "ffac537e6cbbf934b08745a378932722df287a53"],
                'projektorPublish'
        )

        then:
        publishResults.task(":projektorPublish").outcome == SUCCESS

        and:
        verifyOutputContainsReportLink(publishResults.output, serverUrl, resultsId)

        and:
        List<GroupedResults> resultsRequests = resultsStubber.findResultsRequestBodies()
        resultsRequests.size() == 1

        GitMetadata gitMetadata = resultsRequests[0].metadata?.git
        gitMetadata != null

        gitMetadata.repoName == "projektor/projektor"
        gitMetadata.branchName == "main"
        gitMetadata.projectName == projectName
        gitMetadata.isMainBranch
        gitMetadata.commitSha == "ffac537e6cbbf934b08745a378932722df287a53"
    }
}
