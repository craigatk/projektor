package projektor.plugin.testkit.coverage

import org.gradle.util.GradleVersion
import projektor.plugin.coverage.model.CoverageFilePayload
import projektor.plugin.results.grouped.GroupedResults
import projektor.plugin.testkit.SingleProjectSpec
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static projektor.plugin.CodeUnderTestWriter.writePartialCoverageSpecFile
import static projektor.plugin.CodeUnderTestWriter.writeSecondPartialCoverageSpecFile
import static projektor.plugin.CodeUnderTestWriter.writeSourceCodeFile
import static projektor.plugin.ProjectDirectoryWriter.createIntegrationTestDirectory
import static projektor.plugin.ProjectDirectoryWriter.createSourceDirectory
import static projektor.plugin.ProjectDirectoryWriter.createTestDirectory

class CoverageMultiTaskSpec extends SingleProjectSpec {

    @Override
    boolean includeJacocoPlugin() {
        return true
    }

    @Unroll
    def "should publish coverage results from unit and integration tests to server with Gradle version #gradleVersion"() {
        given:
        buildFile << """
            sourceSets {
                intTest {
                    compileClasspath += sourceSets.main.output
                    runtimeClasspath += sourceSets.main.output
                }
            }
            
            configurations {
                intTestImplementation.extendsFrom testImplementation
                intTestRuntimeOnly.extendsFrom runtimeOnly
            }

            task integrationTest(type: Test) {
                description = 'Runs integration tests.'
                group = 'verification'
            
                testClassesDirs = sourceSets.intTest.output.classesDirs
                classpath = sourceSets.intTest.runtimeClasspath
            }

            jacocoTestReport {
                dependsOn test, integrationTest
                executionData { [test, integrationTest].findAll { it.jacoco.destinationFile.exists() }*.jacoco.destinationFile }
            }

            projektor {
                serverUrl = '${serverUrl}'
            }
        """.stripIndent()

        String publicId = "COV123"
        resultsStubber.stubResultsPostSuccess(publicId)

        File sourceDir = createSourceDirectory(projectRootDir)
        File testDir = createTestDirectory(projectRootDir)
        File integrationTestDir = createIntegrationTestDirectory(projectRootDir)

        writeSourceCodeFile(sourceDir)
        writePartialCoverageSpecFile(testDir, "PartialSpec")
        writeSecondPartialCoverageSpecFile(integrationTestDir, "PartialIntegrationSpec")

        when:
        def result = runSuccessfulBuildWithEnvironmentAndGradleVersion(
                ["CI": "true"],
                gradleVersion,
                'test', 'jacocoTestReport', '-i'
        )

        then:
        result.task(":test").outcome == SUCCESS
        result.task(":integrationTest").outcome == SUCCESS
        result.task(":jacocoTestReport").outcome == SUCCESS

        and:
        List<GroupedResults> resultsRequestBodies = resultsStubber.findResultsRequestBodies()
        resultsRequestBodies.size() == 1

        List<CoverageFilePayload> coverageFilePayloads = resultsRequestBodies[0].coverageFiles
        coverageFilePayloads.size() == 1

        coverageFilePayloads[0].reportContents.contains("MyClass")
        coverageFilePayloads[0].reportContents.contains('<counter type="LINE" missed="0" covered="2"/>')

        where:
        gradleVersion                  | _
        GradleVersion.version("7.6.1") | _
        GradleVersion.current()        | _
    }
}
