package projektor.plugin.client

class ClientConfig {
    final String serverUrl
    final Optional<String> maybePublishToken

    ClientConfig(String serverUrl, Optional<String> maybePublishToken) {
        this.serverUrl = serverUrl
        this.maybePublishToken = maybePublishToken
    }
}
