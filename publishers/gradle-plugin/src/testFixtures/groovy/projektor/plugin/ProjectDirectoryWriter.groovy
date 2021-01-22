package projektor.plugin

import org.junit.rules.TemporaryFolder

class ProjectDirectoryWriter {
    static File createSourceDirectory(TemporaryFolder projectDir) {
        projectDir.newFolder("src", "main", "groovy", "projektor")
    }

    static File createSourceDirectory(File projectDir) {
        return createProjectDir(projectDir, "src/main/groovy/projektor")
    }

    static File createTestDirectory(TemporaryFolder projectDir) {
        projectDir.newFolder("src", "test", "groovy", "projektor")
    }

    static File createIntegrationTestDirectory(TemporaryFolder projectDir) {
        projectDir.newFolder("src", "intTest", "groovy", "projektor")
    }

    static File createTestDirectory(File projectDir) {
        return createProjectDir(projectDir, "src/test/groovy/projektor")
    }

    private static File createProjectDir(File projectDir, String dirPath) {
        File dir = new File(projectDir, dirPath)
        dir.mkdirs()
        return dir
    }
}
