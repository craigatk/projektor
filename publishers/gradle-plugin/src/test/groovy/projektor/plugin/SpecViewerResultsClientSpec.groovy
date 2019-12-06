package projektor.plugin

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.gradle.api.logging.Logger
import org.junit.Rule
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

class ProjektorResultsClientSpec extends Specification {

    @Rule
    WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort())

    WireMockStubber wireMockStubber = new WireMockStubber(wireMockRule)

    Logger logger = Mock()

    void "should send results to server"() {
        given:
        String serverUrl = wireMockStubber.serverUrl

        ProjektorResultsClient resultsClient = new ProjektorResultsClient(serverUrl, logger)

        String resultsBlob = """<testsuites>
</testsuites>"""

        String resultsId = "ABC123"
        wireMockStubber.stubResultsPostSuccess(resultsId)

        when:
        PublishResult publishResult = resultsClient.sendResultsToServer(resultsBlob)

        then:
        publishResult.successful

        and:
        List<LoggedRequest> resultsRequests = wireMockStubber.findResultsRequests()
        resultsRequests.size() == 1

        resultsRequests[0].bodyAsString == resultsBlob
    }
}
