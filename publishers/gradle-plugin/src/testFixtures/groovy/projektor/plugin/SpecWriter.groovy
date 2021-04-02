package projektor.plugin

import static projektor.plugin.ProjectDirectoryWriter.createTestDirectory

class SpecWriter {
    static void writeFailingSpecFile(File testDirectory, String specClassName) {
        writeFailingSpecFiles(testDirectory, [specClassName])
    }

    static void writeFailingSpecFiles(File testDirectory, List<String> specClassNames) {
        specClassNames.each { writeSpecFile(testDirectory, it, new SpecFileConfig(passing: false)) }
    }

    static void writePassingSpecFiles(File testDirectory, List<String> specClassNames) {
        specClassNames.each { writePassingSpecFile(testDirectory, it) }
    }

    static void writePassingSpecFile(File testDirectory, String specClassName) {
        writeSpecFile(testDirectory, specClassName, new SpecFileConfig(passing: true))
    }

    static void writeSpecFile(File testDirectory, String specClassName, SpecFileConfig config) {
        File specFile = new File(testDirectory, "${specClassName}.groovy")

        specFile << """package projektor

import spock.lang.Specification

class ${specClassName} extends Specification {
    void "sample test"() {
        expect:
        ${config.additionalCodeLines.join("\n")}
        ${config.passing}
    }
}
"""
    }

    static File createTestDirectoryWithFailingTest(TempDirectory projectDir,  String specClassName) {
        return createTestDirectoryWithFailingTests(projectDir, [specClassName])
    }

    static File createTestDirectoryWithFailingTests(TempDirectory projectDir,  List<String> specClassNames) {
        File testDirectory = createTestDirectory(projectDir)
        specClassNames.each { writeFailingSpecFile(testDirectory, it) }
        return testDirectory
    }

    static File createTestDirectoryWithPassingTest(TempDirectory projectDir,  String specClassName) {
        File testDirectory = createTestDirectory(projectDir)
        writePassingSpecFile(testDirectory, specClassName)
        return testDirectory
    }
}

class SpecFileConfig {
    boolean passing
    List<String> additionalCodeLines = []
}
