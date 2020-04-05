package projektor.plugin.client

class ClientConfig {
    final String serverUrl
    final boolean compressionEnabled
    final Optional<String> maybePublishToken

    ClientConfig(String serverUrl, boolean compressionEnabled, Optional<String> maybePublishToken) {
        this.serverUrl = serverUrl
        this.compressionEnabled = compressionEnabled
        this.maybePublishToken = maybePublishToken
    }
}
