package projektor.plugin

class BuildFileWriter {
    static File createProjectBuildFile(
            TempDirectory projectDir,
            boolean includeProjektorPlugin = true,
            boolean includeJacocoPlugin = false
    ) {
        File buildFile = projectDir.newFile('build.gradle')

        writeBuildFileContents(buildFile, includeProjektorPlugin, includeJacocoPlugin)

        return buildFile
    }

    static void writeBuildFileContents(
            File buildFile,
            boolean includeProjektorPlugin = true,
            boolean includeJacocoPlugin = false
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
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                implementation('org.codehaus.groovy:groovy-all:2.5.13')

                testImplementation('org.spockframework:spock-core:1.3-groovy-2.5')
            }

            ${includeJacocoPlugin ? "jacocoTestReport { dependsOn test }": ""}
        """.stripIndent()
    }

    static File createRootBuildFile(TempDirectory projectRootDir) {
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
