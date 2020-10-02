package projektor.plugin.testkit.attachments

import com.github.tomakehurst.wiremock.verification.LoggedRequest
import projektor.plugin.AttachmentsWireMockStubber
import projektor.plugin.BuildFileWriter
import projektor.plugin.SpecWriter
import projektor.plugin.testkit.ProjectSpec
import projektor.plugin.AttachmentsWriter

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static projektor.plugin.PluginOutput.verifyOutputContainsReportLink
import static projektor.plugin.PluginOutput.verifyOutputDoesNotContainReportLink

class AttachmentsSingleProjectSpec extends ProjectSpec {
    AttachmentsWireMockStubber attachmentsStubber = new AttachmentsWireMockStubber(wireMockRule)

    File buildFile

    def setup() {
        buildFile = BuildFileWriter.createProjectBuildFile(projectRootDir)
    }

    def "should publish results and attachments to server"() {
        given:
        File attachmentsDir1 = AttachmentsWriter.createAttachmentsDir(projectRootDir, 'attachments1')
        AttachmentsWriter.writeAttachmentFile(attachmentsDir1, "attachment1.txt", "Here is attachment 1")
        AttachmentsWriter.writeAttachmentFile(attachmentsDir1, "attachment2.txt", "Here is attachment 2")
        File attachmentsDir2 = AttachmentsWriter.createAttachmentsDir(projectRootDir, 'attachments2')
        AttachmentsWriter.writeAttachmentFile(attachmentsDir2, "attachment3.txt", "Here is attachment 3")

        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                attachments = [fileTree(dir: 'attachments1', include: '**/*.txt'), fileTree(dir: 'attachments2', include: '**/*.txt')]
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithFailingTest(projectRootDir, "SampleSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)
        attachmentsStubber.stubAttachmentPostSuccess(resultsId, "attachment1.txt")
        attachmentsStubber.stubAttachmentPostSuccess(resultsId, "attachment2.txt")
        attachmentsStubber.stubAttachmentPostSuccess(resultsId, "attachment3.txt")

        when:
        def result = runFailedLocalBuild('test')

        then:
        result.task(":test").outcome == FAILED

        and:
        !result.output.contains("Projektor plugin enabled but no server specified")
        verifyOutputContainsReportLink(result.output, serverUrl, resultsId)

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1

        and:
        List<LoggedRequest> attachmentRequests = attachmentsStubber.findAttachmentsRequests(resultsId)
        attachmentRequests.size() == 3
    }

    def "when build passes should not upload attachments"() {
        given:
        File attachmentsDir1 = AttachmentsWriter.createAttachmentsDir(projectRootDir, 'attachments1')
        AttachmentsWriter.writeAttachmentFile(attachmentsDir1, "attachment1.txt", "Here is attachment 1")
        AttachmentsWriter.writeAttachmentFile(attachmentsDir1, "attachment2.txt", "Here is attachment 2")
        File attachmentsDir2 = AttachmentsWriter.createAttachmentsDir(projectRootDir, 'attachments2')
        AttachmentsWriter.writeAttachmentFile(attachmentsDir2, "attachment3.txt", "Here is attachment 3")

        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                attachments = [fileTree(dir: 'attachments1', include: '**/*.txt'), fileTree(dir: 'attachments2', include: '**/*.txt')]
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithPassingTest(projectRootDir, "SampleSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)
        attachmentsStubber.stubAttachmentPostSuccess(resultsId, "attachment1.txt")
        attachmentsStubber.stubAttachmentPostSuccess(resultsId, "attachment2.txt")
        attachmentsStubber.stubAttachmentPostSuccess(resultsId, "attachment3.txt")

        when:
        def result = runSuccessfulLocalBuild('test')

        then:
        result.task(":test").outcome == SUCCESS

        and:
        verifyOutputDoesNotContainReportLink(result.output)

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 0

        and:
        List<LoggedRequest> attachmentRequests = attachmentsStubber.findAnyAttachmentRequests()
        attachmentRequests.size() == 0
    }

    def "should publish results and attachments with publish task"() {
        given:
        File attachmentsDir = AttachmentsWriter.createAttachmentsDir(projectRootDir, 'attachments')
        AttachmentsWriter.writeAttachmentFile(attachmentsDir, "attachment1.txt", "Here is attachment 1")
        AttachmentsWriter.writeAttachmentFile(attachmentsDir, "attachment2.txt", "Here is attachment 2")

        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                alwaysPublish = false
                publishOnLocalFailure = false
                attachments = [fileTree(dir: 'attachments', include: '**/*.txt')]
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithFailingTest(projectRootDir, "SampleSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)
        attachmentsStubber.stubAttachmentPostSuccess(resultsId, "attachment1.txt")
        attachmentsStubber.stubAttachmentPostSuccess(resultsId, "attachment2.txt")

        when:
        def testResult = runFailedLocalBuild('test')

        then:
        testResult.task(":test").outcome == FAILED

        and:
        resultsStubber.findResultsRequests().size() == 0

        when:
        def publishResults = runSuccessfulLocalBuild('publishResults', '--info')

        then:
        publishResults.task(":publishResults").outcome == SUCCESS

        and:
        verifyOutputContainsReportLink(publishResults.output, serverUrl, resultsId)

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1

        and:
        List<LoggedRequest> attachmentRequests = attachmentsStubber.findAttachmentsRequests(resultsId)
        attachmentRequests.size() == 2
    }
}
