package projektor.plugin.testkit

import static projektor.plugin.ProjectDirectoryWriter.createTestDirectory

class MultiProjectWithPluginAppliedToSubprojectsSpec extends ProjectSpec {
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

    String getAdditionalPluginConfig() {
        ""
    }

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

        rootBuildFile = projectRootDir.newFile('build.gradle')
        rootBuildFile << """
            buildscript {
                repositories {
                    mavenCentral()
                }
            }
        """.stripIndent()

        writeSubProjectBuildFile(buildFileProject1)
        writeSubProjectBuildFile(buildFileProject2)
        writeSubProjectBuildFile(buildFileProject3)

        testDirectory1 = createTestDirectory(projectDir1)
        testDirectory2 = createTestDirectory(projectDir2)
        testDirectory3 = createTestDirectory(projectDir3)
    }

    protected void writeSubProjectBuildFile(File buildFile) {
        buildFile << """
            buildscript {
                repositories {
                    mavenCentral()
                }
            }

            plugins {
                id 'groovy'
                id 'dev.projektor.publish'
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                testImplementation('org.spockframework:spock-core:2.3-groovy-3.0')
            }

            test {
                useJUnitPlatform()
            }
            
            projektor {
                serverUrl = '${serverUrl}'
                ${additionalPluginConfig}
            }
        """.stripIndent()
    }
}
