package projektor.plugin.functionaltest

import okhttp3.ResponseBody
import org.gradle.testkit.runner.TaskOutcome
import projektor.plugin.AttachmentsWriter
import projektor.plugin.BuildFileWriter
import projektor.plugin.SpecWriter
import projektor.server.api.TestRun
import projektor.server.api.attachments.Attachments
import retrofit2.Response

class SingleProjectAttachmentsFunctionalSpec extends ProjektorPluginFunctionalSpecification {
    File buildFile

    def setup() {
        buildFile = BuildFileWriter.createProjectBuildFile(projectRootDir)
    }

    def "should attach files from test run"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${PROJEKTOR_SERVER_URL}'
                attachments = [fileTree(dir: 'attachments1', include: '**/*.txt')]
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithFailingTest(projectRootDir, "FailingSpec")

        File attachmentsDir1 = AttachmentsWriter.createAttachmentsDir(projectRootDir, 'attachments1')
        AttachmentsWriter.writeAttachmentFile(attachmentsDir1, "attachment1.txt", "Here is attachment 1")
        AttachmentsWriter.writeAttachmentFile(attachmentsDir1, "attachment2.txt", "Here is attachment 2")

        when:
        def result = runFailedLocalBuild('test')

        then:
        result.task(":test").outcome == TaskOutcome.FAILED

        when:
        String testId = extractTestId(result.output)

        Response<TestRun> testRunResponse = projektorTestRunApi.testRun(testId).execute()

        then:
        testRunResponse.successful

        when:
        Response<Attachments> attachmentsResponse = projektorAttachmentsApi.listAttachments(testId).execute()

        then:
        attachmentsResponse.successful

        Attachments attachments = attachmentsResponse.body()
        attachments.attachments.size() == 2

        attachments.attachments.find { it.fileName == "attachment1.txt" }
        attachments.attachments.find { it.fileName == "attachment2.txt" }

        when:
        Response<ResponseBody> getAttachment1Response = projektorAttachmentsApi.getAttachments(testId, "attachment1.txt").execute()

        then:
        getAttachment1Response.isSuccessful()

        getAttachment1Response.body().string() == "Here is attachment 1"

        when:
        Response<ResponseBody> getAttachment2Response = projektorAttachmentsApi.getAttachments(testId, "attachment2.txt").execute()

        then:
        getAttachment2Response.isSuccessful()

        getAttachment2Response.body().string() == "Here is attachment 2"
    }
}
