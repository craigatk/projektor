package projektor.plugin.quality

import org.gradle.api.file.FileTree
import org.gradle.api.logging.Logger

class CodeQualityCollector {
    private final Logger logger

    CodeQualityCollector(Logger logger) {
        this.logger = logger
    }

    List<CodeQualityFilePayload> collectCodeQualityFiles(List<FileTree> codeQualityReportFileTrees) {
        List<File> codeQualityFiles = codeQualityReportFileTrees.collect { it.files }.flatten()

        if (codeQualityReportFileTrees.size() > 0) {
            logger.info("Found ${codeQualityFiles.size()} code quality files from configured file trees ${codeQualityReportFileTrees}")
        }

        return codeQualityFiles.collect {
            new CodeQualityFilePayload(
                    contents: it.text,
                    fileName: it.name
            )
        }
    }
}
