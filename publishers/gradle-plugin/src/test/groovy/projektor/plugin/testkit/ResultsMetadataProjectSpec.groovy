package projektor.plugin.testkit

import projektor.plugin.SpecWriter
import projektor.plugin.results.grouped.GroupedResults
import projektor.plugin.results.grouped.ResultsMetadata
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static projektor.plugin.PluginOutput.verifyOutputContainsReportLink

class ResultsMetadataProjectSpec extends SingleProjectSpec {
    @Unroll
    def "should include group name when group results is #groupResults with environment #environment"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                alwaysPublish = true
                groupResults = $groupResults
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithPassingTest(projectRootDir, "SamplePassingSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = runSuccessfulBuildWithEnvironment(
                environment,
                'test'
        )

        then:
        result.task(":test").outcome == SUCCESS

        and:
        verifyOutputContainsReportLink(result.output, serverUrl, resultsId)

        and:
        List<GroupedResults> resultsRequests = resultsStubber.findResultsRequestBodies()
        resultsRequests.size() == 1

        ResultsMetadata resultsMetadata = resultsRequests[0].metadata
        resultsMetadata.group == expectedGroup

        where:
        groupResults | environment                 || expectedGroup
        true         | ["GITHUB_RUN_NUMBER": "43"] || "43"
        false        | ["GITHUB_RUN_NUMBER": "43"] || null
    }

    @Unroll
    def "should publish that build is in CI #shouldBeCI"() {
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
                ['CI': ciEnvValue],
                'test'
        )

        then:
        result.task(":test").outcome == SUCCESS

        and:
        verifyOutputContainsReportLink(result.output, serverUrl, resultsId)

        and:
        List<GroupedResults> resultsRequests = resultsStubber.findResultsRequestBodies()
        resultsRequests.size() == 1

        ResultsMetadata resultsMetadata = resultsRequests[0].metadata
        resultsMetadata.ci == shouldBeCI

        where:
        ciEnvValue || shouldBeCI
        "true"     || true
        "false"    || false
    }
}
