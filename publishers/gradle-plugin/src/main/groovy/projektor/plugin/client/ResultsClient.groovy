package projektor.plugin.client

import groovy.json.JsonSlurper
import io.github.resilience4j.retry.Retry
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import org.gradle.api.logging.Logger
import projektor.plugin.PublishResult
import projektor.plugin.results.grouped.GroupedResults

class ResultsClient extends AbstractClient {

    private final PayloadSerializer payloadSerializer = new PayloadSerializer()

    private final Retry publishRetry

    ResultsClient(ClientConfig clientConfig, Logger logger) {
        super(clientConfig, logger)
        this.publishRetry = ClientFactory.createClientRetry(clientConfig, "publish")
    }

    PublishResult sendResultsToServer(GroupedResults groupedResults) {
        PublishResult publishResult = new PublishResult()

        MediaType mediaType = MediaType.get("application/json")

        String groupedResultsJson = payloadSerializer.serializePayload(groupedResults)

        Request request = buildRequest("groupedResults", mediaType, groupedResultsJson)

        try {
            Response response = publishRetry.executeSupplier({ -> client.newCall(request).execute() })

            if (response?.successful) {
                def parsedResponseJson = new JsonSlurper().parseText(response.body().string())
                String testRunUri = parsedResponseJson.uri
                String reportUrl = "${config.serverUrl}${testRunUri}"

                publishResult.publicId = parsedResponseJson.id
                publishResult.reportUrl = reportUrl
            } else {
                String responseCode = response ? "- response code ${response.code()}" : ""
                logger.warn("Failed to upload Projektor report to ${config.serverUrl} ${responseCode}")
            }
        } catch (Exception e) {
            logger.error("Error publishing results to Projektor server ${config.serverUrl} - run with --info to get full stacktrace of the error.")
            logger.info("Error details while attempting to publish results to Projektor server ${config.serverUrl}:", e)
            return publishResult
        }

        return publishResult
    }
}
