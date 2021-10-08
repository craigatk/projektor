package projektor.plugin

import org.junit.rules.TemporaryFolder

class BuildFileWriter {
    static File createProjectBuildFile(
            TemporaryFolder projectDir,
            boolean includeProjektorPlugin = true,
            boolean includeJacocoPlugin = false,
            boolean includeCodeNarcPlugin = false
    ) {
        File buildFile = projectDir.newFile('build.gradle')

        writeBuildFileContents(
                buildFile,
                includeProjektorPlugin,
                includeJacocoPlugin,
                includeCodeNarcPlugin
        )

        return buildFile
    }

    static void writeBuildFileContents(
            File buildFile,
            boolean includeProjektorPlugin = true,
            boolean includeJacocoPlugin = false,
            boolean includeCodeNarcPlugin = false
    ) {
        buildFile << """
            buildscript {
                repositories {
                    mavenCentral()
                }
            }

            plugins {
                id 'groovy'
                ${includeProjektorPlugin ? "id 'dev.projektor.publish'" : ""}
                ${includeJacocoPlugin ? "id 'jacoco'" : ""}
                ${includeCodeNarcPlugin ? "id 'codenarc'" : ""}
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                implementation('org.codehaus.groovy:groovy-all:2.5.13')

                testImplementation('org.spockframework:spock-core:1.3-groovy-2.5')
            }

            ${includeJacocoPlugin ? "jacocoTestReport { dependsOn test }": ""}

            ${includeCodeNarcPlugin ? "codenarc { reportFormat 'text' }": ""}
        """.stripIndent()
    }

    static File createRootBuildFile(TemporaryFolder projectRootDir) {
        File rootBuildFile = projectRootDir.newFile('build.gradle')
        rootBuildFile << """
            buildscript {
                repositories {
                    mavenCentral()
                }
            }

            plugins {
                id 'dev.projektor.publish'
            }
        """.stripIndent()

        return rootBuildFile
    }
}
