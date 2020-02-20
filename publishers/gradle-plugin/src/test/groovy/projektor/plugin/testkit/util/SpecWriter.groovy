package projektor.plugin.testkit.util

import org.junit.rules.TemporaryFolder

class SpecWriter {
    File createTestDirectory(TemporaryFolder projectDir) {
        projectDir.newFolder("src", "test", "groovy", "projektor")
    }

    File createTestDirectory(File projectDir) {
        return createProjectDir(projectDir, "src/test/groovy/projektor")
    }

    private File createProjectDir(File projectDir, String dirPath) {
        File dir = new File(projectDir, dirPath)
        dir.mkdirs()
        return dir
    }

    File writeFailingSpecFile(File testDirectory, String specClass) {
        writeSpecFile(testDirectory, specClass, false)
    }

    File writeSpecFile(File testDirectory, String specClass, boolean passing = true) {
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
}
