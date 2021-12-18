package projektor.plugin.coverage.model

class CoverageFilePayload {
    String reportContents
    String baseDirectoryPath // Relative path from the project's base dir to the base of the sources directory - ex. "src/main/groovy
}
