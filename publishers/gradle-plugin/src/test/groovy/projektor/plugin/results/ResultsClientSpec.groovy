package projektor.plugin.results

import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import okhttp3.OkHttpClient
import org.gradle.api.logging.Logger
import org.junit.Rule
import projektor.plugin.ResultsWireMockStubber
import projektor.plugin.PublishResult
import projektor.plugin.client.ClientConfig
import projektor.plugin.client.ClientToken
import projektor.plugin.results.grouped.GroupedResults
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

class ResultsClientSpec extends Specification {

    @Rule
    WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort())

    ResultsWireMockStubber resultsStubber = new ResultsWireMockStubber(wireMockRule)

    Logger logger = Mock()

    OkHttpClient okHttpClient = new OkHttpClient()

    void "should send results to server without token in header"() {
        given:
        String serverUrl = resultsStubber.serverUrl

        ResultsClient resultsClient = new ResultsClient(
                okHttpClient,
                new ClientConfig(serverUrl, Optional.empty()),
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
    }

    void "should send results to server with token in header"() {
        given:
        String serverUrl = resultsStubber.serverUrl

        ResultsClient resultsClient = new ResultsClient(
                okHttpClient,
                new ClientConfig(serverUrl, Optional.of("token12345")),
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
}
