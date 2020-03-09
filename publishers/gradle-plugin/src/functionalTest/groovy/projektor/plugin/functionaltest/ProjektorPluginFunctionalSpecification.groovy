package projektor.plugin.functionaltest

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import projektor.plugin.SpecWriter
import projektor.server.client.ProjektorAttachmentsApi
import projektor.server.client.ProjektorClientBuilder
import projektor.server.client.ProjektorTestRunApi
import spock.lang.Specification

class ProjektorPluginFunctionalSpecification extends Specification {

    static final String PROJEKTOR_SERVER_URL = "http://localhost:8084"

    SpecWriter specWriter = new SpecWriter()

    OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .addInterceptor(createLoggingInterceptor())
            .build()

    ProjektorAttachmentsApi projektorAttachmentsApi = ProjektorClientBuilder.INSTANCE.createApi(
            PROJEKTOR_SERVER_URL,
            okHttpClient,
            ProjektorAttachmentsApi.class
    )

    ProjektorTestRunApi projektorTestRunApi = ProjektorClientBuilder.INSTANCE.createApi(
            PROJEKTOR_SERVER_URL,
            okHttpClient,
            ProjektorTestRunApi.class
    )

    static String extractTestId(String output) {
        String reportMessage = "View Projektor report at: ${PROJEKTOR_SERVER_URL}/tests/"
        assert output.contains(reportMessage)
        int startingIndex = output.indexOf(reportMessage) + reportMessage.size()

        return output.substring(startingIndex, startingIndex + 12)
    }

    private static HttpLoggingInterceptor createLoggingInterceptor() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return logging
    }
}
