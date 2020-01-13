package projektor.plugin.testkit

import org.gradle.testkit.runner.GradleRunner

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class PublishingResultsFailsSpec extends SingleProjectSpec {
    def "when publishing fails with networking error to server"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        File testDirectory = specWriter.createTestDirectory(projectRootDir)
        specWriter.writeSpecFile(testDirectory, "SampleSpec")

        wireMockStubber.stubResultsNetworkingError()

        when:
        def result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test')
                .withPluginClasspath()
                .build()

        then:
        result.task(":test").outcome == SUCCESS
    }
}
