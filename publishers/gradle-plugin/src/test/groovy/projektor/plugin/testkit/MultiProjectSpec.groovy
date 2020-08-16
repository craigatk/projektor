package projektor.plugin.testkit

import projektor.plugin.BuildFileWriter

import static projektor.plugin.ProjectDirectoryWriter.createTestDirectory

class MultiProjectSpec extends ProjectSpec {
    File projectDir1
    File projectDir2
    File projectDir3

    File buildFileProject1
    File buildFileProject2
    File buildFileProject3

    File rootBuildFile

    File settingsFile

    File testDirectory1
    File testDirectory2
    File testDirectory3

    def setup() {
        settingsFile = projectRootDir.newFile('settings.gradle')
        settingsFile << """
include 'project1', 'project2', 'project3'
"""
        projectDir1 = projectRootDir.newFolder('project1')
        projectDir2 = projectRootDir.newFolder('project2')
        projectDir3 = projectRootDir.newFolder('project3')

        buildFileProject1 = new File(projectDir1, "build.gradle")
        buildFileProject2 = new File(projectDir2, "build.gradle")
        buildFileProject3 = new File(projectDir3, "build.gradle")

        rootBuildFile = BuildFileWriter.createRootBuildFile(projectRootDir)

        BuildFileWriter.writeBuildFileContents(buildFileProject1, false, includeJacocoPlugin())
        BuildFileWriter.writeBuildFileContents(buildFileProject2, false, includeJacocoPlugin())
        BuildFileWriter.writeBuildFileContents(buildFileProject3, false, includeJacocoPlugin())

        testDirectory1 = createTestDirectory(projectDir1)
        testDirectory2 = createTestDirectory(projectDir2)
        testDirectory3 = createTestDirectory(projectDir3)
    }
}
