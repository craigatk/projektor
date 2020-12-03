package projektor.plugin

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import projektor.parser.coverage.payload.CoverageFilePayload
import projektor.parser.coverage.payload.CoveragePayloadParser

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.post
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo

class CoverageWireMockStubber extends WireMockStubber {
    CoverageWireMockStubber(WireMockServer wireMockServer) {
        super(wireMockServer)
    }

    void stubCoveragePostSuccess(String publicId) {
        wireMockServer.stubFor(post(urlEqualTo(coverageUrl(publicId))).willReturn(aResponse()
                .withStatus(200)))
    }

    void stubCoveragePostFailure(String publicId, int statusCode) {
        wireMockServer.stubFor(post(urlEqualTo(coverageUrl(publicId))).willReturn(aResponse()
                .withStatus(statusCode)))
    }

    List<LoggedRequest> findCoverageRequests(String publicId) {
        wireMockServer.findRequestsMatching(
                postRequestedFor(urlEqualTo(coverageUrl(publicId))).build()
        ).requests
    }

    List<CoverageFilePayload> findCoveragePayloads(String publicId) {
        CoveragePayloadParser coveragePayloadParser = new CoveragePayloadParser()
        findCoverageRequests(publicId).collect { coveragePayloadParser.parseCoverageFilePayload(it.bodyAsString) }
    }

    private static String coverageUrl(String publicId) {
        return "/run/${publicId}/coverageFile"
    }
}
