package projektor.plugin.functionaltest

import projektor.plugin.BuildFileWriter
import projektor.plugin.CodenarcWriter
import projektor.server.api.TestRun
import projektor.server.api.quality.CodeQualityReport
import projektor.server.api.quality.CodeQualityReports
import retrofit2.Response
import spock.util.concurrent.PollingConditions

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static projektor.plugin.CodeUnderTestWriter.writeFullCoverageSpecFile
import static projektor.plugin.CodeUnderTestWriter.writeSourceCodeFile
import static projektor.plugin.ProjectDirectoryWriter.createSourceDirectory
import static projektor.plugin.ProjectDirectoryWriter.createTestDirectory

class CodeQualitySingleProjectFunctionalSpec extends ProjektorPluginFunctionalSpecification {
    File buildFile

    def setup() {
        buildFile = BuildFileWriter.createProjectBuildFile(
                projectRootDir,
                true,
                false,
                true
        )
    }

    def "should publish code quality file"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${PROJEKTOR_SERVER_URL}'
                codeQualityReports = [fileTree(dir: 'build/reports/codenarc/', include: '*.txt')]
            }
        """.stripIndent()

        CodenarcWriter.writeCodenarcConfigFile(projectRootDir)

        File sourceDir = createSourceDirectory(projectRootDir)
        File testDir = createTestDirectory(projectRootDir)

        writeSourceCodeFile(sourceDir)
        writeFullCoverageSpecFile(testDir, "FullSpec")

        when:
        def result = runFailedBuildInCI('codenarcTest', '-i')

        then:
        result.task(":codenarcTest").outcome == FAILED

        when:
        String testId = extractTestId(result.output)

        then:
        new PollingConditions().eventually {
            Response<TestRun> testRunResponse = projektorTestRunApi.testRun(testId).execute()
            assert testRunResponse.successful
        }

        new PollingConditions().eventually {
            Response<CodeQualityReports> codeQualityReportsResponse = projektorTestRunApi.codeQualityReports(testId).execute()
            assert codeQualityReportsResponse.successful
            assert codeQualityReportsResponse.body()

            List<CodeQualityReport> reports = codeQualityReportsResponse.body().reports
            assert reports.size() == 1

            assert reports[0].contents.contains("CodeNarc Report")
            assert reports[0].fileName == "test.txt"
        }
    }
}
