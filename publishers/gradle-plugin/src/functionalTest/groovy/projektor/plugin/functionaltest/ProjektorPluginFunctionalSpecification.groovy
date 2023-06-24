package projektor.plugin.functionaltest

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import projektor.plugin.PluginOutput
import projektor.server.client.ProjektorAttachmentsApi
import projektor.server.client.ProjektorClientBuilder
import projektor.server.client.ProjektorTestRunApi
import projektor.server.client.ProjektorTestRunMetadataApi
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class ProjektorPluginFunctionalSpecification extends Specification {

    static final String PROJEKTOR_SERVER_URL = "http://localhost:8092"

    @Rule
    TemporaryFolder projectRootDir = new TemporaryFolder()

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

    ProjektorTestRunMetadataApi projektorTestRunMetadataApi = ProjektorClientBuilder.INSTANCE.createApi(
            PROJEKTOR_SERVER_URL,
            okHttpClient,
            ProjektorTestRunMetadataApi.class
    )

    static String extractTestId(String output) {
        return PluginOutput.extractTestId(output, PROJEKTOR_SERVER_URL)
    }

    BuildResult runPassingBuildInCI(String... buildArgs) {
        runPassingBuildWithEnvironment(["CI": "true"], buildArgs)
    }

    BuildResult runPassingLocalBuild(String... buildArgs) {
        runPassingBuildWithEnvironment(["CI": "false"], buildArgs)
    }

    BuildResult runPassingBuildWithEnvironment(Map<String, String> envMap, String... buildArgs) {
        Map<String, String> currentEnv = System.getenv()
        Map<String, String> augmentedEnv = new HashMap<>(currentEnv)
        augmentedEnv.putAll(envMap)

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withEnvironment(augmentedEnv)
                .withArguments(buildArgs)
                .withPluginClasspath()
                .build()

        println result.output

        return result
    }

    BuildResult runFailedBuildInCI(String... buildArgs) {
        runFailedBuildWithEnvironment(["CI": "true"], buildArgs)
    }

    BuildResult runFailedLocalBuild(String... buildArgs) {
        runFailedBuildWithEnvironment(["CI": "false"], buildArgs)
    }

    BuildResult runFailedBuildWithEnvironment(Map<String, String> envMap, String... buildArgs) {
        Map<String, String> currentEnv = System.getenv()
        Map<String, String> augmentedEnv = new HashMap<>(currentEnv)
        augmentedEnv.putAll(envMap)

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withEnvironment(augmentedEnv)
                .withArguments(buildArgs)
                .withPluginClasspath()
                .buildAndFail()

        println result.output

        return result
    }

    void waitForTestRunProcessingToComplete(String testId) {
        new PollingConditions().eventually {
            assert projektorTestRunApi.testRun(testId).execute().successful
        }
    }

    private static HttpLoggingInterceptor createLoggingInterceptor() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return logging
    }
}
