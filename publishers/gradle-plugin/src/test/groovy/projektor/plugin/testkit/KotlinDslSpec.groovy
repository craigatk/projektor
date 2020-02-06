package projektor.plugin.testkit

import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.gradle.testkit.runner.GradleRunner

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class KotlinDslSpec extends ProjectSpec {
    File buildFile

    def setup() {
        buildFile = projectRootDir.newFile('build.gradle.kts')
        buildFile << """
            buildscript {
                repositories {
                    jcenter()
                }
            }

            plugins {
                id("groovy")
                id("dev.projektor.publish")
            }
            
            repositories {
                jcenter()
            }
            
            dependencies {
                "testImplementation"("org.spockframework:spock-core:1.3-groovy-2.5")
            }
            
             configure<projektor.plugin.ProjektorPublishPluginExtension> {
                serverUrl = "${serverUrl}"
            }
        """.stripIndent()
    }

    def "should publish results from test task to server"() {
        given:
        File testDirectory = specWriter.createTestDirectory(projectRootDir)
        specWriter.writeSpecFile(testDirectory, "SampleSpec")

        String resultsId = "ABC123"
        wireMockStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test')
                .withPluginClasspath()
                .build()

        then:
        result.task(":test").outcome == SUCCESS

        and:
        !result.output.contains("Projektor plugin enabled but no server specified")
        result.output.contains("View Projektor report at: ${serverUrl}/tests/${resultsId}")

        and:
        List<LoggedRequest> resultsRequests = wireMockStubber.findResultsRequests()
        resultsRequests.size() == 1
    }
}
