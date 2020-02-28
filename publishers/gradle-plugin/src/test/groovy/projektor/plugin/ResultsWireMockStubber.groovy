package projektor.plugin

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.verification.LoggedRequest

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.post
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo

class ResultsWireMockStubber extends WireMockStubber {
    ResultsWireMockStubber(WireMockServer wireMockServer) {
        super(wireMockServer)
    }

    void stubResultsPostSuccess(String resultsId) {
        wireMockServer.stubFor(post(urlEqualTo("/groupedResults")).willReturn(aResponse()
                .withStatus(200)
                .withBody("""{"id": "${resultsId}", "uri": "/tests/${resultsId}"}""")))
    }

    void stubResultsPostFailure(int statusCode) {
        wireMockServer.stubFor(post(urlEqualTo("/groupedResults")).willReturn(aResponse()
                .withStatus(statusCode)))
    }

    void stubResultsNetworkingError() {
        wireMockServer.stubFor(post(urlEqualTo("/groupedResults")).willReturn(aResponse()
                .withFault(Fault.CONNECTION_RESET_BY_PEER)
        ))
    }

    List<LoggedRequest> findResultsRequests() {
        wireMockServer.findRequestsMatching(
                postRequestedFor(urlEqualTo("/groupedResults")).build()
        ).requests
    }
}
