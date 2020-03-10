package projektor.plugin.testkit.attachments

import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.gradle.testkit.runner.GradleRunner
import projektor.plugin.AttachmentsWireMockStubber
import projektor.plugin.testkit.SingleProjectSpec
import projektor.plugin.AttachmentsWriter

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class AttachmentsSingleProjectSpec extends SingleProjectSpec {
    AttachmentsWireMockStubber attachmentsStubber = new AttachmentsWireMockStubber(wireMockRule)

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

        File testDirectory = specWriter.createTestDirectory(projectRootDir)
        specWriter.writeFailingSpecFile(testDirectory, "SampleSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)
        attachmentsStubber.stubAttachmentPostSuccess(resultsId, "attachment1.txt")
        attachmentsStubber.stubAttachmentPostSuccess(resultsId, "attachment2.txt")
        attachmentsStubber.stubAttachmentPostSuccess(resultsId, "attachment3.txt")

        when:
        def result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test')
                .withPluginClasspath()
                .buildAndFail()

        then:
        result.task(":test").outcome == FAILED

        and:
        !result.output.contains("Projektor plugin enabled but no server specified")
        result.output.contains("View Projektor report at: ${serverUrl}/tests/${resultsId}")

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

        File testDirectory = specWriter.createTestDirectory(projectRootDir)
        specWriter.writeSpecFile(testDirectory, "SampleSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)
        attachmentsStubber.stubAttachmentPostSuccess(resultsId, "attachment1.txt")
        attachmentsStubber.stubAttachmentPostSuccess(resultsId, "attachment2.txt")
        attachmentsStubber.stubAttachmentPostSuccess(resultsId, "attachment3.txt")

        when:
        def result = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test')
                .withPluginClasspath()
                .build()

        then:
        result.task(":test").outcome == SUCCESS

        and:
        !result.output.contains("View Projektor report")

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
                autoPublish = false
                attachments = [fileTree(dir: 'attachments', include: '**/*.txt')]
            }
        """.stripIndent()

        File testDirectory = specWriter.createTestDirectory(projectRootDir)
        specWriter.writeFailingSpecFile(testDirectory, "SampleSpec")

        String resultsId = "ABC123"
        resultsStubber.stubResultsPostSuccess(resultsId)
        attachmentsStubber.stubAttachmentPostSuccess(resultsId, "attachment1.txt")
        attachmentsStubber.stubAttachmentPostSuccess(resultsId, "attachment2.txt")

        when:
        def testResult = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('test')
                .withPluginClasspath()
                .buildAndFail()

        then:
        testResult.task(":test").outcome == FAILED

        and:
        resultsStubber.findResultsRequests().size() == 0

        when:
        def publishResults = GradleRunner.create()
                .withProjectDir(projectRootDir.root)
                .withArguments('publishResults', '--info')
                .withPluginClasspath()
                .build()

        println publishResults.output

        then:
        publishResults.task(":publishResults").outcome == SUCCESS

        and:
        publishResults.output.contains("View Projektor report at: ${serverUrl}/tests/${resultsId}")

        and:
        List<LoggedRequest> resultsRequests = resultsStubber.findResultsRequests()
        resultsRequests.size() == 1

        and:
        List<LoggedRequest> attachmentRequests = attachmentsStubber.findAttachmentsRequests(resultsId)
        attachmentRequests.size() == 2
    }
}
