package projektor.plugin.results

import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.gradle.api.logging.Logger
import org.junit.Rule
import projektor.plugin.WireMockStubber
import projektor.plugin.PublishResult
import projektor.plugin.results.grouped.GroupedResults
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import static projektor.plugin.results.ProjektorResultsClient.getPUBLISH_TOKEN_NAME

class ProjektorResultsClientSpec extends Specification {

    @Rule
    WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort())

    WireMockStubber wireMockStubber = new WireMockStubber(wireMockRule)

    Logger logger = Mock()

    void "should send results to server without token in header"() {
        given:
        String serverUrl = wireMockStubber.serverUrl

        ProjektorResultsClient resultsClient = new ProjektorResultsClient(
                new ResultsClientConfig(serverUrl, Optional.empty()),
                logger
        )

        GroupedResults groupedResults = new GroupedResults(
                groupedTestSuites: []
        )

        String resultsId = "ABC123"
        wireMockStubber.stubResultsPostSuccess(resultsId)

        when:
        PublishResult publishResult = resultsClient.sendResultsToServer(groupedResults)

        then:
        publishResult.successful

        and:
        List<LoggedRequest> resultsRequests = wireMockStubber.findResultsRequests()
        resultsRequests.size() == 1

        resultsRequests[0].bodyAsString == """{"groupedTestSuites":[]}"""
    }

    void "should send results to server with token in header"() {
        given:
        String serverUrl = wireMockStubber.serverUrl

        ProjektorResultsClient resultsClient = new ProjektorResultsClient(
                new ResultsClientConfig(serverUrl, Optional.of("token12345")),
                logger
        )

        GroupedResults groupedResults = new GroupedResults(
                groupedTestSuites: []
        )

        String resultsId = "ABC123"
        wireMockStubber.stubResultsPostSuccess(resultsId)

        when:
        PublishResult publishResult = resultsClient.sendResultsToServer(groupedResults)

        then:
        publishResult.successful

        and:
        List<LoggedRequest> resultsRequests = wireMockStubber.findResultsRequests()
        resultsRequests.size() == 1
        LoggedRequest resultsRequest = resultsRequests[0]

        resultsRequest.bodyAsString == """{"groupedTestSuites":[]}"""

        HttpHeader publishTokenInHeader = resultsRequest.header(PUBLISH_TOKEN_NAME)
        publishTokenInHeader.firstValue() == "token12345"
    }
}
