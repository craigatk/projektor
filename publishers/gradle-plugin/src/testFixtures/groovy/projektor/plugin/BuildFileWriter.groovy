package projektor.plugin

import org.junit.rules.TemporaryFolder

class BuildFileWriter {
    static File createProjectBuildFile(TemporaryFolder projectDir) {
        File buildFile = projectDir.newFile('build.gradle')

        writeBuildFileContents(buildFile)

        return buildFile
    }

    static void writeBuildFileContents(File buildFile, boolean includeProjektorPlugin = true) {
        buildFile << """
            buildscript {
                repositories {
                    jcenter()
                }
            }

            plugins {
                id 'groovy'
                ${includeProjektorPlugin ? "id 'dev.projektor.publish'" : ""}
            }
            
            repositories {
                jcenter()
            }
            
            dependencies {
                testImplementation('org.spockframework:spock-core:1.3-groovy-2.5')
            }
        """.stripIndent()
    }

    static File createRootBuildFile(TemporaryFolder projectRootDir) {
        File rootBuildFile = projectRootDir.newFile('build.gradle')
        rootBuildFile << """
            buildscript {
                repositories {
                    jcenter()
                }
            }

            plugins {
                id 'dev.projektor.publish'
            }
        """.stripIndent()

        return rootBuildFile
    }
}
