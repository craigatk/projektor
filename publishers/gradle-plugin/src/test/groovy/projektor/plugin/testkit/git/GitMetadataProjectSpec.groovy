package projektor.plugin.testkit.git

import projektor.plugin.SpecWriter
import projektor.plugin.results.grouped.GitMetadata
import projektor.plugin.results.grouped.GroupedResults
import projektor.plugin.testkit.SingleProjectSpec

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static projektor.plugin.PluginOutput.verifyOutputContainsReportLink

class GitMetadataProjectSpec extends SingleProjectSpec {
    def "should publish Git repo and branch info metadata along with results"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                alwaysPublish = true
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithPassingTest(projectRootDir, "SamplePassingSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = runSuccessfulBuildWithEnvironment(
                ['GITHUB_REPOSITORY': 'projektor/projektor', "GITHUB_REF": "refs/head/main"],
                'test'
        )

        then:
        result.task(":test").outcome == SUCCESS

        and:
        verifyOutputContainsReportLink(result.output, serverUrl, resultsId)

        and:
        List<GroupedResults> resultsRequests = resultsStubber.findResultsRequestBodies()
        resultsRequests.size() == 1

        GitMetadata gitMetadata = resultsRequests[0].metadata.git

        gitMetadata.repoName == "projektor/projektor"
        gitMetadata.branchName == "main"
        gitMetadata.isMainBranch == true
    }
}
