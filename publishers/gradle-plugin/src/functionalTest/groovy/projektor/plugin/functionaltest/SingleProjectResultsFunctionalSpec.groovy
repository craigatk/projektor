package projektor.plugin.functionaltest

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import projektor.plugin.SpecWriter
import spock.lang.Specification

class SingleProjectResultsFunctionalSpec extends Specification {
    @Rule
    TemporaryFolder projectRootDir = new TemporaryFolder()

    SpecWriter specWriter = new SpecWriter()

    File buildFile

    def setup() {
        buildFile = projectRootDir.newFile('build.gradle')
        buildFile << """
            buildscript {
                repositories {
                    jcenter()
                }
            }

            plugins {
                id 'groovy'
                id 'dev.projektor.publish'
            }
            
            repositories {
                jcenter()
            }
            
            dependencies {
                testImplementation('org.spockframework:spock-core:1.3-groovy-2.5')
            }
        """.stripIndent()
    }

    def "should send results from single project to server"() {
        given:
        buildFile << """
            projektor {
                serverUrl = 'http://localhost:8084'
            }
        """.stripIndent()

        File testDirectory = specWriter.createTestDirectory(projectRootDir)
        specWriter.writeSpecFile(testDirectory, "SampleSpec")

        OkHttpClient okHttpClient = new OkHttpClient.Builder().build()

        when:
        def result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test')
                .withPluginClasspath()
                .build()

        then:
        result.task(":test").outcome == TaskOutcome.SUCCESS

        when:
        String testId = extractTestId(result.output)

        Request.Builder getTestRunRequestBuilder = new Request.Builder()
                .url("http://localhost:8084/run/${testId}")
                .get()

        and:
        Response response = okHttpClient.newCall(getTestRunRequestBuilder.build()).execute()

        then:
        response.successful
    }

    static String extractTestId(String output) {
        String reportMessage = "View Projektor report at: http://localhost:8084/tests/"
        assert output.contains(reportMessage)
        int startingIndex = output.indexOf(reportMessage) + reportMessage.size()

        return output.substring(startingIndex, startingIndex + 12)
    }
}
