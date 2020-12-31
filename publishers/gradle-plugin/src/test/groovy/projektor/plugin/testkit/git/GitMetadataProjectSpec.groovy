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
                ['GITHUB_REPOSITORY': 'projektor/projektor', "GITHUB_REF": "refs/head/main", "GITHUB_SHA": "ffac537e6cbbf934b08745a378932722df287a53"],
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
        gitMetadata.isMainBranch
        gitMetadata.commitSha == "ffac537e6cbbf934b08745a378932722df287a53"
    }

    def "should publish branch name when a branch name environment variable is set"() {
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
                ['GITHUB_REPOSITORY': 'projektor/projektor', "VELA_PULL_REQUEST_SOURCE": "main"],
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
        gitMetadata.isMainBranch
    }

    def "should publish pull request number when it is set"() {
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
                ['VELA_REPO_FULL_NAME': 'projektor/projektor', "VELA_PULL_REQUEST_SOURCE": "feature-branch", "VELA_BUILD_PULL_REQUEST": "42"],
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
        gitMetadata.branchName == "feature-branch"
        !gitMetadata.isMainBranch
        gitMetadata.pullRequestNumber == 42
    }
}
