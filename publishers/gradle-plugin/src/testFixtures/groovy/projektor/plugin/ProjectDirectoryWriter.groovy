package projektor.plugin

class ProjectDirectoryWriter {
    static File createSourceDirectory(TempDirectory projectDir) {
        projectDir.newDirectory("src/main/groovy/projektor")
    }

    static File createSourceDirectory(File projectDir) {
        return createProjectDir(projectDir, "src/main/groovy/projektor")
    }

    static File createTestDirectory(TempDirectory projectDir) {
        projectDir.newDirectory("src/test/groovy/projektor")
    }

    static File createIntegrationTestDirectory(TempDirectory projectDir) {
        projectDir.newDirectory("src/intTest/groovy/projektor")
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
