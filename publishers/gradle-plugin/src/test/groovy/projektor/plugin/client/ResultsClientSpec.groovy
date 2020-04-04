package projektor.plugin.client

import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.gradle.api.logging.Logger
import org.junit.Rule
import projektor.plugin.ResultsWireMockStubber
import projektor.plugin.PublishResult
import projektor.plugin.results.grouped.GroupedResults
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

class ResultsClientSpec extends Specification {

    @Rule
    WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort())

    ResultsWireMockStubber resultsStubber = new ResultsWireMockStubber(wireMockRule)

    Logger logger = Mock()

    void "should send results to server without token in header"() {
        given:
        String serverUrl = resultsStubber.serverUrl

        ResultsClient resultsClient = new ResultsClient(
                new ClientConfig(serverUrl, true, Optional.empty()),
                logger
        )

        GroupedResults groupedResults = new GroupedResults(
                groupedTestSuites: []
        )

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        PublishResult publishResult = resultsClient.sendResultsToServer(groupedResults)

        then:
        publishResult.successful
        publishResult.publicId == "ABC123"

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1

        resultsRequests[0].bodyAsString == """{"groupedTestSuites":[]}"""

        !resultsRequests[0].containsHeader(ClientToken.PUBLISH_TOKEN_NAME)
    }

    void "should send results to server with token in header"() {
        given:
        String serverUrl = resultsStubber.serverUrl

        ResultsClient resultsClient = new ResultsClient(
                new ClientConfig(serverUrl, true, Optional.of("token12345")),
                logger
        )

        GroupedResults groupedResults = new GroupedResults(
                groupedTestSuites: []
        )

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        PublishResult publishResult = resultsClient.sendResultsToServer(groupedResults)

        then:
        publishResult.successful
        publishResult.publicId == "ABC123"

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1
        LoggedRequest resultsRequest = resultsRequests[0]

        resultsRequest.bodyAsString == """{"groupedTestSuites":[]}"""

        HttpHeader publishTokenInHeader = resultsRequest.header(ClientToken.PUBLISH_TOKEN_NAME)
        publishTokenInHeader.firstValue() == "token12345"
    }

    void "should not stacktrace when offline"() {
        given:
        String serverUrl = "http://resolve.failure.fakedotcom:9999/womp"
        ResultsClient resultsClient = new ResultsClient(
                new ClientConfig(serverUrl, true, Optional.empty()),
                logger
        )
        GroupedResults groupedResults = new GroupedResults()

        when:
        PublishResult publishResult = resultsClient.sendResultsToServer(groupedResults)

        then:
        !publishResult.successful
    }

    void "when compression enabled should include gzip header"() {
        given:
        String serverUrl = resultsStubber.serverUrl

        ResultsClient resultsClient = new ResultsClient(
                new ClientConfig(serverUrl, true, Optional.empty()),
                logger
        )

        GroupedResults groupedResults = new GroupedResults(
                groupedTestSuites: []
        )

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        PublishResult publishResult = resultsClient.sendResultsToServer(groupedResults)

        then:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1
        resultsRequests[0].header("Content-Encoding").firstValue() == 'gzip'
    }

    void "when compression not enabled should not include gzip header"() {
        given:
        String serverUrl = resultsStubber.serverUrl

        ResultsClient resultsClient = new ResultsClient(
                new ClientConfig(serverUrl, false, Optional.empty()),
                logger
        )

        GroupedResults groupedResults = new GroupedResults(
                groupedTestSuites: []
        )

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        PublishResult publishResult = resultsClient.sendResultsToServer(groupedResults)

        then:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1
        !resultsRequests[0].containsHeader("Content-Encoding")
    }
}
