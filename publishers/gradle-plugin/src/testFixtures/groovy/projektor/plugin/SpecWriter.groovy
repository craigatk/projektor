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

    static File writeFailingSpecFile(File testDirectory, String specClass) {
        writeSpecFile(testDirectory, specClass, false)
    }

    static File writeSpecFile(File testDirectory, String specClass, boolean passing = true) {
        File specFile = new File(testDirectory, "${specClass}.groovy")

        specFile << """package projektor

import spock.lang.Specification

class ${specClass} extends Specification {
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
        return writeSpecFile(testDirectory, specClassName)
    }
}
