package projektor.plugin.testkit.kotlin

import com.github.tomakehurst.wiremock.verification.LoggedRequest
import projektor.plugin.SpecWriter
import projektor.plugin.testkit.ProjectSpec

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static projektor.plugin.PluginOutput.verifyOutputContainsReportLink

class KotlinDslSpec extends ProjectSpec {
    File buildFile

    def setup() {
        buildFile = projectRootDir.newFile('build.gradle.kts')
        buildFile << """
            buildscript {
                repositories {
                    mavenCentral()
                }
            }

            plugins {
                id("groovy")
                id("dev.projektor.publish")
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                "testImplementation"("org.spockframework:spock-core:2.4-M5-groovy-4.0")
            }

            tasks.named<Test>("test") {
                useJUnitPlatform()
            }
            
             configure<projektor.plugin.ProjektorPublishPluginExtension> {
                serverUrl = "${serverUrl}"
            }
        """.stripIndent()
    }

    def "should publish results from test task to server"() {
        given:
        SpecWriter.createTestDirectoryWithFailingTest(projectRootDir, "SampleSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        def result = runFailedLocalBuild('test')

        then:
        result.task(":test").outcome == FAILED

        and:
        verifyOutputContainsReportLink(result.output, serverUrl, resultsId)

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1
    }
}
