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
import spock.lang.Unroll

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
                new ClientConfig(serverUrl, true, Optional.empty(), 1, 0, 10_000),
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
        !resultsRequests[0].containsHeader(ClientToken.PUBLISH_TOKEN_NAME)

        List<GroupedResults> resultsBodies = resultsStubber.findResultsRequestBodies()
        resultsBodies.size() == 1
        resultsBodies[0].groupedTestSuites == []
    }

    void "should send results to server with token in header"() {
        given:
        String serverUrl = resultsStubber.serverUrl

        ResultsClient resultsClient = new ResultsClient(
                new ClientConfig(serverUrl, true, Optional.of("token12345"), 1, 0, 10_000),
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

        HttpHeader publishTokenInHeader = resultsRequest.header(ClientToken.PUBLISH_TOKEN_NAME)
        publishTokenInHeader.firstValue() == "token12345"

        and:
        List<GroupedResults> resultsBodies = resultsStubber.findResultsRequestBodies()
        resultsBodies.size() == 1
        resultsBodies[0].groupedTestSuites == []
    }

    @Unroll
    void "when sending results returns response code #responseCode should retry #expectedRetries"() {
        given:
        String serverUrl = resultsStubber.serverUrl

        ResultsClient resultsClient = new ResultsClient(
                new ClientConfig(serverUrl, true, Optional.empty(), retryMaxAttempts, 0, 1000),
                logger
        )

        GroupedResults groupedResults = new GroupedResults(
                groupedTestSuites: []
        )

        resultsStubber.stubResultsPostFailure(responseCode)

        when:
        resultsClient.sendResultsToServer(groupedResults)

        then:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == expectedRetries

        where:
        responseCode | retryMaxAttempts || expectedRetries
        400          | 3                || 3
        500          | 2                || 2
        401          | 3                || 1
        200          | 3                || 1
    }

    void "when compression enabled should include gzip header"() {
        given:
        String serverUrl = resultsStubber.serverUrl

        ResultsClient resultsClient = new ResultsClient(
                new ClientConfig(serverUrl, true, Optional.empty(), 1, 0, 10_000),
                logger
        )

        GroupedResults groupedResults = new GroupedResults(
                groupedTestSuites: []
        )

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        resultsClient.sendResultsToServer(groupedResults)

        then:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1
        resultsRequests[0].header("Content-Encoding").firstValue() == 'gzip'
    }

    void "when compression not enabled should not include gzip header"() {
        given:
        String serverUrl = resultsStubber.serverUrl

        ResultsClient resultsClient = new ResultsClient(
                new ClientConfig(serverUrl, false, Optional.empty(), 1, 0, 10_000),
                logger
        )

        GroupedResults groupedResults = new GroupedResults(
                groupedTestSuites: []
        )

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        resultsClient.sendResultsToServer(groupedResults)

        then:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1
        !resultsRequests[0].containsHeader("Content-Encoding")
    }
}
