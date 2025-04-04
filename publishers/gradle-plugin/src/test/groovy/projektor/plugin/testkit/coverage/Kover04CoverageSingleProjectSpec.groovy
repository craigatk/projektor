package projektor.plugin.testkit.coverage

import org.gradle.testkit.runner.BuildResult
import org.gradle.util.GradleVersion
import projektor.plugin.ProjektorPluginVersion
import projektor.plugin.coverage.model.CoverageFilePayload
import projektor.plugin.results.grouped.GroupedResults
import projektor.plugin.testkit.SingleProjectSpec
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static projektor.plugin.CodeUnderTestWriter.writeKotlinSourceCodeFile
import static projektor.plugin.CodeUnderTestWriter.writePartialCoverageKotestFile
import static projektor.plugin.CodeUnderTestWriter.writeResourcesFile
import static projektor.plugin.ProjectDirectoryWriter.createKotlinSourceDirectory
import static projektor.plugin.ProjectDirectoryWriter.createKotlinTestDirectory
import static projektor.plugin.ProjectDirectoryWriter.createResourcesDirectory

class Kover04CoverageSingleProjectSpec extends SingleProjectSpec {

    @Override
    boolean includeKoverPlugin() {
        return true
    }

    @Unroll
    def "should publish Kover coverage results to server with Gradle version #gradleVersion"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        String publicId = "COV123"
        resultsStubber.stubResultsPostSuccess(publicId)

        File sourceDir = createKotlinSourceDirectory(projectRootDir)
        File testDir = createKotlinTestDirectory(projectRootDir)

        writeKotlinSourceCodeFile(sourceDir)
        writePartialCoverageKotestFile(testDir, "FooTest")

        when:
        BuildResult result = runSuccessfulBuildWithEnvironmentAndGradleVersion(
                ["CI": "true"],
                gradleVersion,
                'test', 'koverXmlReport', '-i'
        )

        then:
        result.task(":test").outcome == SUCCESS
        result.task(":koverXmlReport").outcome == SUCCESS

        and:
        List<GroupedResults> resultsRequestBodies = resultsStubber.findResultsRequestBodies()
        resultsRequestBodies.size() == 1

        List<CoverageFilePayload> coverageFilePayloads = resultsRequestBodies[0].coverageFiles
        coverageFilePayloads.size() == 1

        coverageFilePayloads[0].reportContents.contains("Foo.kt")
        coverageFilePayloads[0].baseDirectoryPath == "src/main/kotlin"

        where:
        gradleVersion                  | _
        GradleVersion.version(ProjektorPluginVersion.MINIMUM_GRADLE_VERSION) | _
        GradleVersion.current()        | _
    }

    def "should filter out resources directory from source directory list and publish coverage with base directory path"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        String publicId = "COV123"
        resultsStubber.stubResultsPostSuccess(publicId)

        File sourceDir = createKotlinSourceDirectory(projectRootDir)
        File testDir = createKotlinTestDirectory(projectRootDir)

        writeKotlinSourceCodeFile(sourceDir)
        writePartialCoverageKotestFile(testDir, "FooTest")

        and:
        File resourcesDir = createResourcesDirectory(projectRootDir)
        writeResourcesFile(resourcesDir)

        when:
        BuildResult result = runSuccessfulBuildInCI('test', 'koverXmlReport', '-i')

        then:
        result.task(":test").outcome == SUCCESS
        result.task(":koverXmlReport").outcome == SUCCESS

        and:
        List<GroupedResults> resultsRequestBodies = resultsStubber.findResultsRequestBodies()
        resultsRequestBodies.size() == 1
        List<CoverageFilePayload> coverageFilePayloads = resultsRequestBodies[0].coverageFiles
        coverageFilePayloads.size() == 1

        coverageFilePayloads[0].reportContents.contains("Foo.kt")
        coverageFilePayloads[0].baseDirectoryPath == "src/main/kotlin"
    }

    def "when coverage disabled should not publish coverage results to server"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                codeCoveragePublish = false
            }
        """.stripIndent()

        String publicId = "COV123"
        resultsStubber.stubResultsPostSuccess(publicId)

        File sourceDir = createKotlinSourceDirectory(projectRootDir)
        File testDir = createKotlinTestDirectory(projectRootDir)

        writeKotlinSourceCodeFile(sourceDir)
        writePartialCoverageKotestFile(testDir, "FooTest")

        when:
        def result = runSuccessfulBuildInCI('test', 'koverXmlReport', '-i')

        then:
        result.task(":test").outcome == SUCCESS
        result.task(":koverXmlReport").outcome == SUCCESS

        and:
        List<GroupedResults> resultsRequestBodies = resultsStubber.findResultsRequestBodies()
        resultsRequestBodies.size() == 1

        List<CoverageFilePayload> coverageFilePayloads = resultsRequestBodies[0].coverageFiles
        coverageFilePayloads.size() == 0
    }

    def "can execute 'tasks' task without failing"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        File sourceDir = createKotlinSourceDirectory(projectRootDir)
        File testDir = createKotlinTestDirectory(projectRootDir)

        writeKotlinSourceCodeFile(sourceDir)
        writePartialCoverageKotestFile(testDir, "FooTest")

        when:
        def result = runSuccessfulBuildInCI('tasks')

        then:
        result.task(":tasks").outcome == SUCCESS
    }

    def "should not fail to publish when kover report file is deleted"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
            }
            
            task deleteKoverReports(type: Delete) {
                mustRunAfter koverXmlReport
                dependsOn koverXmlReport
                
                delete "build/reports/kover/report.xml"
            }
        """.stripIndent()

        String publicId = "COV123"
        resultsStubber.stubResultsPostSuccess(publicId)

        File sourceDir = createKotlinSourceDirectory(projectRootDir)
        File testDir = createKotlinTestDirectory(projectRootDir)

        writeKotlinSourceCodeFile(sourceDir)
        writePartialCoverageKotestFile(testDir, "FooTest")

        when:
        BuildResult result = runSuccessfulBuildInCI('test', 'koverXmlReport', 'deleteKoverReports', '-i', '--stacktrace')

        then:
        result.task(":test").outcome == SUCCESS
        result.task(":koverXmlReport").outcome == SUCCESS

        and:
        List<GroupedResults> resultsRequestBodies = resultsStubber.findResultsRequestBodies()
        resultsRequestBodies.size() == 1

        List<CoverageFilePayload> coverageFilePayloads = resultsRequestBodies[0].coverageFiles
        coverageFilePayloads.size() == 0
    }
}
