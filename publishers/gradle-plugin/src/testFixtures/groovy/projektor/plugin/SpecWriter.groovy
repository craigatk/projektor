package projektor.plugin

import org.junit.rules.TemporaryFolder

class SpecWriter {
    static File createTestDirectory(TemporaryFolder projectDir) {
        projectDir.newFolder("src", "test", "groovy", "projektor")
    }

    static File createTestDirectory(File projectDir) {
        return createProjectDir(projectDir, "src/test/groovy/projektor")
    }

    private static File createProjectDir(File projectDir, String dirPath) {
        File dir = new File(projectDir, dirPath)
        dir.mkdirs()
        return dir
    }

    static void writeFailingSpecFile(File testDirectory, String specClassName) {
        writeFailingSpecFiles(testDirectory, [specClassName])
    }

    static void writeFailingSpecFiles(File testDirectory, List<String> specClassNames) {
        specClassNames.each { writeSpecFile(testDirectory, it, false) }
    }

    static void writePassingSpecFiles(File testDirectory, List<String> specClassNames) {
        specClassNames.each { writePassingSpecFile(testDirectory, it) }
    }

    static void writePassingSpecFile(File testDirectory, String specClassName) {
        writeSpecFile(testDirectory, specClassName, true)
    }

    private static void writeSpecFile(File testDirectory, String specClassName, boolean passing) {
        File specFile = new File(testDirectory, "${specClassName}.groovy")

        specFile << """package projektor

import spock.lang.Specification

class ${specClassName} extends Specification {
    void "sample test"() {
        expect:
        ${passing}
    }
}
"""
    }

    static File createTestDirectoryWithFailingTest(TemporaryFolder projectDir,  String specClassName) {
        return createTestDirectoryWithFailingTests(projectDir, [specClassName])
    }

    static File createTestDirectoryWithFailingTests(TemporaryFolder projectDir,  List<String> specClassNames) {
        File testDirectory = createTestDirectory(projectDir)
        specClassNames.each { writeFailingSpecFile(testDirectory, it) }
        return testDirectory
    }

    static File createTestDirectoryWithPassingTest(TemporaryFolder projectDir,  String specClassName) {
        File testDirectory = createTestDirectory(projectDir)
        return writePassingSpecFile(testDirectory, specClassName)
    }
}
