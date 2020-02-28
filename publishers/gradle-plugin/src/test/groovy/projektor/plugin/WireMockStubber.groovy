package projektor.plugin

import com.github.tomakehurst.wiremock.WireMockServer

class WireMockStubber {
    protected final WireMockServer wireMockServer

    WireMockStubber(WireMockServer wireMockServer) {
        this.wireMockServer = wireMockServer
    }

    String getServerUrl() {
        "http://localhost:${wireMockServer.port()}"
    }
}
