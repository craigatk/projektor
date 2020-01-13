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
    private final String serverUrl
    private final Logger logger
    private final GroupedResultsSerializer groupedResultsSerializer = new GroupedResultsSerializer()

    ProjektorResultsClient(String serverUrl, Logger logger) {
        this.serverUrl = serverUrl
        this.logger = logger
    }

    PublishResult sendResultsToServer(GroupedResults groupedResults) {
        PublishResult publishResult = new PublishResult()

        MediaType mediaType = MediaType.get("application/json")

        String groupedResultsJson = groupedResultsSerializer.serializeGroupedResults(groupedResults)

        OkHttpClient client = new OkHttpClient()

        RequestBody body = RequestBody.create(mediaType, groupedResultsJson)

        Request request = new Request.Builder()
                .url("${serverUrl}/groupedResults")
                .post(body)
                .build()

        Response response = client.newCall(request).execute()

        if (response.successful) {
            String testRunUri = new JsonSlurper().parseText(response.body().string()).uri
            String reportUrl = "${serverUrl}${testRunUri}"

            publishResult.reportUrl = reportUrl
        } else {
            logger.warn("Failed to upload Projektor report to ${serverUrl}")
        }

        return publishResult
    }
}
