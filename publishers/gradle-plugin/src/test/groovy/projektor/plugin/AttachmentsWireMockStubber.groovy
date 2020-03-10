package projektor.plugin

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.verification.LoggedRequest

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.post
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching

class AttachmentsWireMockStubber extends WireMockStubber {
    AttachmentsWireMockStubber(WireMockServer wireMockServer) {
        super(wireMockServer)
    }

    void stubAttachmentPostSuccess(String publicId, String attachmentName) {
        wireMockServer.stubFor(post(urlEqualTo("/run/${publicId}/attachments/${attachmentName}")).willReturn(aResponse()
                .withStatus(200)))
    }

    List<LoggedRequest> findAttachmentsRequests(String publicId) {
        wireMockServer.findRequestsMatching(
                postRequestedFor(urlMatching("/run/${publicId}/attachments/.*")).build()
        ).requests
    }

    List<LoggedRequest> findAnyAttachmentRequests() {
        wireMockServer.findRequestsMatching(
                postRequestedFor(urlMatching("/run/.*/attachments/.*")).build()
        ).requests
    }
}
