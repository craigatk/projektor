package projektor.plugin.client

import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import okhttp3.OkHttpClient
import okhttp3.Response

import java.time.Duration
import java.util.concurrent.TimeUnit

class ClientFactory {
    static OkHttpClient createClient(ClientConfig clientConfig) {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
        okHttpClientBuilder.retryOnConnectionFailure = false
        okHttpClientBuilder.callTimeout(clientConfig.timeout, TimeUnit.MILLISECONDS)
        okHttpClientBuilder.readTimeout(clientConfig.timeout, TimeUnit.MILLISECONDS)
        okHttpClientBuilder.connectTimeout(clientConfig.timeout, TimeUnit.MILLISECONDS)

        return okHttpClientBuilder.build()
    }

    static Retry createClientRetry(ClientConfig clientConfig, String name) {
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(clientConfig.retryMaxAttempts)
                .waitDuration(Duration.ofMillis(clientConfig.retryInterval))
                .retryOnResult({ Response response -> !response.successful && response.code() != 401 })
                .retryOnException({ Throwable e -> true })
                .build()

        RetryRegistry registry = RetryRegistry.of(retryConfig)

        return registry.retry(name)
    }
}
