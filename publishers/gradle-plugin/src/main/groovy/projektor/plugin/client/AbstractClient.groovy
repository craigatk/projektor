package projektor.plugin.client

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.gradle.api.logging.Logger

import static projektor.plugin.client.ClientToken.conditionallyAddPublishTokenToRequest

abstract class AbstractClient {
    protected final ClientConfig config
    protected final Logger logger
    protected final OkHttpClient client

    AbstractClient(ClientConfig clientConfig, Logger logger) {
        this.config = clientConfig
        this.logger = logger

        this.client = ClientFactory.createClient(clientConfig)
    }

    protected Request buildRequest(String partialUrl, MediaType mediaType, String requestBody) {
        RequestBody body = config.compressionEnabled
                ? RequestBody.create(mediaType, CompressionUtil.gzip(requestBody))
                : RequestBody.create(mediaType, requestBody)

        String fullUrl = "${config.serverUrl}/${partialUrl}"

        Request.Builder requestBuilder = new Request.Builder()
                .url(fullUrl)
                .post(body)

        if (config.compressionEnabled) {
            requestBuilder.header("Content-Encoding", "gzip")
        }

        conditionallyAddPublishTokenToRequest(requestBuilder, config)

        return requestBuilder.build()
    }
}
