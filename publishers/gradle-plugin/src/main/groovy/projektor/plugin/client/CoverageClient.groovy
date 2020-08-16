package projektor.plugin.client

import io.github.resilience4j.retry.Retry
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.gradle.api.logging.Logger

import static projektor.plugin.client.ClientToken.conditionallyAddPublishTokenToRequest

class CoverageClient {
    private final ClientConfig config
    private final Logger logger
    private final OkHttpClient client
    private final Retry publishRetry

    CoverageClient(ClientConfig clientConfig, Logger logger) {
        this.config = clientConfig
        this.logger = logger

        this.client = ClientFactory.createClient(clientConfig)
        this.publishRetry = ClientFactory.createClientRetry(clientConfig, "coverage")
    }

    void sendCoverageToServer(File coverageFile, String publicId) {
        MediaType mediaType = MediaType.get("text/plain")

        RequestBody body = RequestBody.create(mediaType, coverageFile.text)

        String coverageUrl = "${config.serverUrl}/run/$publicId/coverage"
        Request.Builder requestBuilder = new Request.Builder()
                .url(coverageUrl)
                .post(body)

        conditionallyAddPublishTokenToRequest(requestBuilder, config)

        Request request = requestBuilder.build()

        try {
            Response response = publishRetry.executeSupplier({ -> client.newCall(request).execute() })

            if (!response?.successful) {
                String responseCode = response ? "- response code ${response.code()}" : ""
                logger.warn("Failed to upload test coverage to Projektor ${responseCode}")
            }
        } catch (Exception e) {
            logger.error("Error publishing test coverage to Projektor server ${config.serverUrl} - run with --info to get full stacktrace of the error.")
            logger.info("Error details while attempting to publish test coverage to Projektor server ${config.serverUrl}:", e)
        }
    }
}
