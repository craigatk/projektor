package projektor.plugin

class AttachmentsWriter {
    static File createAttachmentsDir(TempDirectory projectDir, String dirName) {
        projectDir.newDirectory(dirName)
    }

    static File createAttachmentsDir(File projectDir, String dirName) {
        File dir = new File(projectDir, dirName)
        dir.mkdirs()
        return dir
    }

    static File writeAttachmentFile(File attachmentsDir, String fileName, String contents) {
        File attachmentFile = new File(attachmentsDir, fileName)
        attachmentFile.createNewFile()

        attachmentFile.text = contents

        return attachmentFile
    }
}