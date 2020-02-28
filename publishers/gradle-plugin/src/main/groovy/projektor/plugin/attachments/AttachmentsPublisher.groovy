package projektor.plugin.attachments

import org.gradle.api.file.FileTree
import org.gradle.api.logging.Logger

class AttachmentsPublisher {
    private final AttachmentsClient attachmentsClient
    private final Logger logger

    AttachmentsPublisher(AttachmentsClient attachmentsClient, Logger logger) {
        this.attachmentsClient = attachmentsClient
        this.logger = logger
    }

    void publishAttachments(String publicId, List<FileTree> attachmentFileTrees) {
        List<File> attachments = attachmentFileTrees.collect { it.files }.flatten()
        logger.info("Sending ${attachments.size()} attachments to Projektor report ${publicId}")

        attachments.each { attachmentsClient.sendAttachmentToServer(publicId, it) }
    }
}
