package projektor.plugin.results

import groovy.json.JsonSlurper
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.gradle.api.logging.Logger
import projektor.plugin.PublishResult
import projektor.plugin.results.grouped.GroupedResults
import projektor.plugin.results.grouped.GroupedResultsSerializer

class ProjektorResultsClient {
    static final String PUBLISH_TOKEN_NAME = "X-PROJEKTOR-TOKEN"

    private final ResultsClientConfig config
    private final Logger logger
    private final GroupedResultsSerializer groupedResultsSerializer = new GroupedResultsSerializer()

    ProjektorResultsClient(ResultsClientConfig resultsClientConfig, Logger logger) {
        this.config = resultsClientConfig
        this.logger = logger
    }

    PublishResult sendResultsToServer(GroupedResults groupedResults) {
        PublishResult publishResult = new PublishResult()

        MediaType mediaType = MediaType.get("application/json")

        String groupedResultsJson = groupedResultsSerializer.serializeGroupedResults(groupedResults)

        OkHttpClient client = new OkHttpClient()

        RequestBody body = RequestBody.create(mediaType, groupedResultsJson)

        Request.Builder requestBuilder = new Request.Builder()
                .url("${config.serverUrl}/groupedResults")
                .post(body)

        config.maybePublishToken.ifPresent( { publishToken -> requestBuilder.header(PUBLISH_TOKEN_NAME, publishToken)})

        Request request = requestBuilder.build()

        Response response = null

        try {
            response = client.newCall(request).execute()
        } catch (Exception e) {
            logger.error("Error publishing results to Projektor server ${config.serverUrl}", e)
        }

        if (response?.successful) {
            String testRunUri = new JsonSlurper().parseText(response.body().string()).uri
            String reportUrl = "${config.serverUrl}${testRunUri}"

            publishResult.reportUrl = reportUrl
        } else {
            String responseCode = response ? "- response code ${response.code()}" : ""
            logger.warn("Failed to upload Projektor report to ${config.serverUrl} ${responseCode}")
        }

        return publishResult
    }
}
