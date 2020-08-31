package projektor.plugin

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer

class WireMockStubber {
    protected final WireMockServer wireMockServer
    protected final ObjectMapper objectMapper = new ObjectMapper()

    WireMockStubber(WireMockServer wireMockServer) {
        this.wireMockServer = wireMockServer
    }

    String getServerUrl() {
        "http://localhost:${wireMockServer.port()}"
    }
}
