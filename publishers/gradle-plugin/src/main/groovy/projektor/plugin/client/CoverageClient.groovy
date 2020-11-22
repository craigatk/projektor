package projektor.plugin.client

import io.github.resilience4j.retry.Retry
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import org.gradle.api.logging.Logger

class CoverageClient extends AbstractClient {
    private final Retry publishRetry

    CoverageClient(ClientConfig clientConfig, Logger logger) {
        super(clientConfig, logger)

        this.publishRetry = ClientFactory.createClientRetry(clientConfig, "coverage")
    }

    void sendCoverageToServer(File coverageFile, String publicId) {
        MediaType mediaType = MediaType.get("text/plain")

        Request request = buildRequest("run/$publicId/coverage", mediaType, coverageFile.text)

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
