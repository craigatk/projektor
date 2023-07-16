package projektor.plugin

import org.junit.rules.TemporaryFolder

class ProjectDirectoryWriter {
    static File createSourceDirectory(TemporaryFolder projectDir) {
        projectDir.newFolder("src", "main", "groovy", "projektor")
    }

    static File createSourceDirectory(File projectDir) {
        return createProjectDir(projectDir, "src/main/groovy/projektor")
    }

    static File createKotlinSourceDirectory(TemporaryFolder projectDir) {
        projectDir.newFolder("src", "main", "kotlin", "projektor")
    }

    static File createKotlinSourceDirectory(File projectDir) {
        return createProjectDir(projectDir, "src/main/kotlin/projektor")
    }

    static File createResourcesDirectory(TemporaryFolder projectDir) {
        projectDir.newFolder("src", "main", "resources")
    }

    static File createResourcesDirectory(File projectDir) {
        return createProjectDir(projectDir, "src/main/resources")
    }

    static File createTestDirectory(TemporaryFolder projectDir) {
        projectDir.newFolder("src", "test", "groovy", "projektor")
    }

    static File createKotlinTestDirectory(TemporaryFolder projectDir) {
        projectDir.newFolder("src", "test", "kotlin", "projektor")
    }

    static File createIntegrationTestDirectory(TemporaryFolder projectDir) {
        projectDir.newFolder("src", "intTest", "groovy", "projektor")
    }

    static File createKotlinIntegrationTestDirectory(TemporaryFolder projectDir) {
        projectDir.newFolder("src", "intTest", "kotlin", "projektor")
    }

    static File createTestDirectory(File projectDir) {
        return createProjectDir(projectDir, "src/test/groovy/projektor")
    }

    static File createKotlinTestDirectory(File projectDir) {
        return createProjectDir(projectDir, "src/test/kotlin/projektor")
    }

    private static File createProjectDir(File projectDir, String dirPath) {
        File dir = new File(projectDir, dirPath)
        dir.mkdirs()
        return dir
    }
}
