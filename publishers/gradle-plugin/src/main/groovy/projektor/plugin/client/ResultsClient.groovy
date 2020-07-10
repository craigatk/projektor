package projektor.plugin.client

import groovy.json.JsonSlurper
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.gradle.api.logging.Logger
import projektor.plugin.PublishResult
import projektor.plugin.results.grouped.GroupedResults
import projektor.plugin.results.grouped.GroupedResultsSerializer

import java.time.Duration
import java.util.concurrent.TimeUnit

import static projektor.plugin.client.ClientToken.conditionallyAddPublishTokenToRequest

class ResultsClient {

    private final ClientConfig config
    private final Logger logger
    private final GroupedResultsSerializer groupedResultsSerializer = new GroupedResultsSerializer()
    private final OkHttpClient client

    private final Retry publishRetry

    ResultsClient(ClientConfig clientConfig, Logger logger) {
        this.config = clientConfig
        this.logger = logger

        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
        okHttpClientBuilder.retryOnConnectionFailure = false
        okHttpClientBuilder.callTimeout(clientConfig.timeout, TimeUnit.MILLISECONDS)
        okHttpClientBuilder.readTimeout(clientConfig.timeout, TimeUnit.MILLISECONDS)
        okHttpClientBuilder.connectTimeout(clientConfig.timeout, TimeUnit.MILLISECONDS)
        this.client = okHttpClientBuilder.build()

        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(clientConfig.retryMaxAttempts)
                .waitDuration(Duration.ofMillis(clientConfig.retryInterval))
                .retryOnResult({ Response response -> !response.successful && response.code() != 401 })
                .retryOnException({ Throwable e -> true })
                .build()

        RetryRegistry registry = RetryRegistry.of(retryConfig)

        this.publishRetry = registry.retry("publish")
    }

    PublishResult sendResultsToServer(GroupedResults groupedResults) {
        PublishResult publishResult = new PublishResult()

        MediaType mediaType = MediaType.get("application/json")

        String groupedResultsJson = groupedResultsSerializer.serializeGroupedResults(groupedResults)

        RequestBody body = config.compressionEnabled
                ? RequestBody.create(mediaType, CompressionUtil.gzip(groupedResultsJson))
                : RequestBody.create(mediaType, groupedResultsJson)

        String resultsUrl = "${config.serverUrl}/groupedResults"
        Request.Builder requestBuilder = new Request.Builder()
                .url(resultsUrl)
                .post(body)

        if (config.compressionEnabled) {
            requestBuilder.header("Content-Encoding", "gzip")
        }

        conditionallyAddPublishTokenToRequest(requestBuilder, config)

        Request request = requestBuilder.build()

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
                logger.warn("Failed to upload Projektor report to ${resultsUrl} ${responseCode}")
            }
        } catch (Exception e) {
            logger.error("Error publishing results to Projektor server ${config.serverUrl} - run with --info to get full stacktrace of the error.")
            logger.info("Error details while attempting to publish results to Projektor server ${config.serverUrl}:", e)
            return publishResult
        }

        return publishResult
    }
}
