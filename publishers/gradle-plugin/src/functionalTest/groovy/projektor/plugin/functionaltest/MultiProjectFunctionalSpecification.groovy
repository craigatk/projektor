package projektor.plugin.functionaltest

import projektor.plugin.BuildFileWriter
import projektor.plugin.ProjectBuildFileConfig

import static projektor.plugin.ProjectDirectoryWriter.createTestDirectory

class MultiProjectFunctionalSpecification extends ProjektorPluginFunctionalSpecification {
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

        ProjectBuildFileConfig config = new ProjectBuildFileConfig(
                includeProjektorPlugin: false,
                includeJacocoPlugin: includeJacocoPlugin(),
                includeKoverPlugin: includeKoverPlugin()
        )

        BuildFileWriter.writeBuildFileContents(buildFileProject1, config)
        BuildFileWriter.writeBuildFileContents(buildFileProject2, config)
        BuildFileWriter.writeBuildFileContents(buildFileProject3, config)

        testDirectory1 = createTestDirectory(projectDir1)
        testDirectory2 = createTestDirectory(projectDir2)
        testDirectory3 = createTestDirectory(projectDir3)
    }

    boolean includeJacocoPlugin() {
        return false
    }

    boolean includeKoverPlugin() {
        return false
    }
}
