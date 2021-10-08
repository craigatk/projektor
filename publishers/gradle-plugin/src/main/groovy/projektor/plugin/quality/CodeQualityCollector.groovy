package projektor.plugin.quality

import org.gradle.api.file.FileTree

class CodeQualityCollector {
    List<CodeQualityFilePayload> collectCodeQualityFiles(List<FileTree> codeQualityReportFileTrees) {
        List<File> codeQualityFiles = codeQualityReportFileTrees.collect { it.files }.flatten()

        return codeQualityFiles.collect {
            new CodeQualityFilePayload(
                    contents: it.text,
                    fileName: it.name
            )
        }
    }
}
