package projektor.plugin.results

import groovy.json.JsonSlurper
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.gradle.api.logging.Logger
import projektor.plugin.PublishResult
import projektor.plugin.client.ClientConfig
import projektor.plugin.results.grouped.GroupedResults
import projektor.plugin.results.grouped.GroupedResultsSerializer

import static projektor.plugin.client.ClientToken.conditionallyAddPublishTokenToRequest

class ResultsClient {

    private final ClientConfig config
    private final Logger logger
    private final GroupedResultsSerializer groupedResultsSerializer = new GroupedResultsSerializer()
    private final OkHttpClient client

    ResultsClient(OkHttpClient client, ClientConfig clientConfig, Logger logger) {
        this.client = client
        this.config = clientConfig
        this.logger = logger
    }

    PublishResult sendResultsToServer(GroupedResults groupedResults) {
        PublishResult publishResult = new PublishResult()

        MediaType mediaType = MediaType.get("application/json")

        String groupedResultsJson = groupedResultsSerializer.serializeGroupedResults(groupedResults)

        RequestBody body = RequestBody.create(mediaType, groupedResultsJson)

        String resultsUrl = "${config.serverUrl}/groupedResults"
        Request.Builder requestBuilder = new Request.Builder()
                .url(resultsUrl)
                .post(body)

        conditionallyAddPublishTokenToRequest(requestBuilder, config)

        Request request = requestBuilder.build()

        try {
            Response response = client.newCall(request).execute()

            if (response?.successful) {
                def parsedResponseJson = new JsonSlurper().parseText(response.body().string())
                String testRunUri = parsedResponseJson.uri
                String reportUrl = "${config.serverUrl}${testRunUri}"

                publishResult.publicId= parsedResponseJson.id
                publishResult.reportUrl = reportUrl
            } else {
                String responseCode = response ? "- response code ${response.code()}" : ""
                logger.warn("Failed to upload Projektor report to ${resultsUrl} ${responseCode}")
            }
        } catch (Exception e) {
            logger.error("Unable to publish results to Projektor server ${resultsUrl}")
            logger.debug("Error details while attempting to publish results to Projektor server ${resultsUrl}:", e)
            return publishResult
        }

        return publishResult
    }
}
