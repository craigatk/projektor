package projektor.plugin

import groovy.json.JsonSlurper
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.gradle.api.logging.Logger

class ProjektorResultsClient {
    private final String serverUrl
    private final Logger logger

    ProjektorResultsClient(String serverUrl, Logger logger) {
        this.serverUrl = serverUrl
        this.logger = logger
    }

    PublishResult sendResultsToServer(String resultsBlob) {
        PublishResult publishResult = new PublishResult()

        MediaType plainText = MediaType.get("text/plain")

        OkHttpClient client = new OkHttpClient()

        RequestBody body = RequestBody.create(plainText, resultsBlob)

        Request request = new Request.Builder()
                .url("${serverUrl}/results")
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
