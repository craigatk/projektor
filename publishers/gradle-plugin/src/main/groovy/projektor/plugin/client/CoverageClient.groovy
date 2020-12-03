package projektor.plugin.client

import io.github.resilience4j.retry.Retry
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import org.gradle.api.logging.Logger
import projektor.plugin.coverage.CodeCoverageFile
import projektor.plugin.coverage.model.CoverageFilePayload

class CoverageClient extends AbstractClient {
    private final PayloadSerializer payloadSerializer = new PayloadSerializer()

    private final Retry publishRetry

    CoverageClient(ClientConfig clientConfig, Logger logger) {
        super(clientConfig, logger)

        this.publishRetry = ClientFactory.createClientRetry(clientConfig, "coverage")
    }

    void sendCoverageToServer(CodeCoverageFile coverageFile, String publicId) {
        MediaType mediaType = MediaType.get("application/json")

        CoverageFilePayload coverageFilePayload = new CoverageFilePayload(
                reportContents: coverageFile.reportFile.text,
                baseDirectoryPath: coverageFile.baseDirectoryPath
        )
        String payloadJson = payloadSerializer.serializePayload(coverageFilePayload)

        Request request = buildRequest("run/$publicId/coverageFile", mediaType, payloadJson)

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
