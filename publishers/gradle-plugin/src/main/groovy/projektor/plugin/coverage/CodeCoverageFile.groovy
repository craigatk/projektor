package projektor.plugin.coverage

class CodeCoverageFile {
    final File reportFile
    final String baseDirectoryPath

    CodeCoverageFile(File reportFile, String baseDirectoryPath) {
        this.reportFile = reportFile
        this.baseDirectoryPath = baseDirectoryPath
    }
}
