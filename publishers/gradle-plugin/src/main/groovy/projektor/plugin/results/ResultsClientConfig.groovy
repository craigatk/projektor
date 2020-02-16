package projektor.plugin.results

class ResultsClientConfig {
    final String serverUrl
    final Optional<String> maybePublishToken

    ResultsClientConfig(String serverUrl, Optional<String> maybePublishToken) {
        this.serverUrl = serverUrl
        this.maybePublishToken = maybePublishToken
    }
}
