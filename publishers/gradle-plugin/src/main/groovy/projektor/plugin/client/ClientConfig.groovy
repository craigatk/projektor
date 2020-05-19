package projektor.plugin.client

class ClientConfig {
    final String serverUrl
    final boolean compressionEnabled
    final Optional<String> maybePublishToken
    final int retryMaxAttempts
    final long retryInterval
    final long timeout

    ClientConfig(
            String serverUrl,
            boolean compressionEnabled,
            Optional<String> maybePublishToken,
            int retryMaxAttempts,
            long retryInterval,
            long timeout
    ) {
        this.serverUrl = serverUrl
        this.compressionEnabled = compressionEnabled
        this.maybePublishToken = maybePublishToken
        this.retryMaxAttempts = retryMaxAttempts
        this.retryInterval = retryInterval
        this.timeout = timeout
    }
}
