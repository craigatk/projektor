package projektor.plugin.testkit.custompath

import com.github.tomakehurst.wiremock.verification.LoggedRequest
import projektor.parser.ResultsXmlLoader
import projektor.plugin.SpecWriter
import projektor.plugin.testkit.SingleProjectSpec
import projektor.plugin.testkit.util.ResultsWriter

import static projektor.plugin.PluginOutput.verifyOutputContainsReportLink

class IncludeCustomResultsPathSpec extends SingleProjectSpec {
    def setup() {
        ResultsWriter resultsWriter = new ResultsWriter()

        File customResultsFolder1 = projectRootDir.newFolder("build", "customResults1")
        resultsWriter.writeResults(customResultsFolder1, "TEST-1-passing.xml", new ResultsXmlLoader().passing())
        resultsWriter.writeResults(customResultsFolder1, "TEST-1-failing.xml", new ResultsXmlLoader().failing())

        File customResultsFolder2 = projectRootDir.newFolder("build", "customResults2")
        resultsWriter.writeResults(customResultsFolder2, "TEST-2-output.xml", new ResultsXmlLoader().output())
    }

    def "when only custom path results paths and no normal test task results should collect results"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                additionalResultsDirs = ['build/customResults1', 'build/customResults2']
                autoPublishOnFailureOnly = false
            }
        """.stripIndent()

        String resultsId = "JKS192"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = runSuccessfulBuild('test')

        then:
        verifyOutputContainsReportLink(result.output, serverUrl, resultsId)

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1

        String resultsBody = resultsRequests[0].bodyAsString
        resultsBody.contains("projektor.example.spock.PassingSpec")
        resultsBody.contains("projektor.example.spock.FailingSpec")
        resultsBody.contains("projektor.example.spock.OutputSpec")
    }

    def "when custom path results paths and normal test task results should collect results from both sources"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                additionalResultsDirs = ['build/customResults1', 'build/customResults2']
            }
        """.stripIndent()

        String resultsId = "IOJ901"
        resultsStubber.stubResultsPostSuccess(resultsId)

        SpecWriter.createTestDirectoryWithFailingTest(projectRootDir, "SampleFailingSpec")

        when:
        def result = runFailedBuild('test')

        then:
        verifyOutputContainsReportLink(result.output, serverUrl, resultsId)

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1

        String resultsBody = resultsRequests[0].bodyAsString
        resultsBody.contains("projektor.example.spock.PassingSpec")
        resultsBody.contains("projektor.example.spock.FailingSpec")
        resultsBody.contains("projektor.example.spock.OutputSpec")

        and:
        resultsBody.contains("SampleFailingSpec")
    }

    def "when custom path results paths and normal test task results and auto-publish disabled should collect results from both sources when task executed"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                additionalResultsDirs = ['build/customResults1', 'build/customResults2']
                autoPublish = false
            }
        """.stripIndent()

        String resultsId = "IOJ901"
        resultsStubber.stubResultsPostSuccess(resultsId)

        SpecWriter.createTestDirectoryWithPassingTest(projectRootDir, "SampleSpec")

        when:
        def result = runSuccessfulBuild('test', 'publishResults', '--info')

        then:
        verifyOutputContainsReportLink(result.output, serverUrl, resultsId)

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1

        String resultsBody = resultsRequests[0].bodyAsString
        resultsBody.contains("projektor.example.spock.PassingSpec")
        resultsBody.contains("projektor.example.spock.FailingSpec")
        resultsBody.contains("projektor.example.spock.OutputSpec")

        and:
        resultsBody.contains("SampleSpec")
    }

    def "when only custom path results paths and no normal test task results but some custom paths do not exist should collect results"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                additionalResultsDirs = ['build/doesNotExist', 'build/customResults1', 'build/customResults2']
                autoPublishOnFailureOnly = false
            }
        """.stripIndent()

        String resultsId = "JKS192"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = runSuccessfulBuild('test')

        then:
        verifyOutputContainsReportLink(result.output, serverUrl, resultsId)

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1

        String resultsBody = resultsRequests[0].bodyAsString
        resultsBody.contains("projektor.example.spock.PassingSpec")
        resultsBody.contains("projektor.example.spock.FailingSpec")
        resultsBody.contains("projektor.example.spock.OutputSpec")
    }
}
