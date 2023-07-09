package projektor.plugin

import org.junit.rules.TemporaryFolder

class BuildFileWriter {
    static File createProjectBuildFile(
            TemporaryFolder projectDir,
            ProjectBuildFileConfig config = new ProjectBuildFileConfig()
    ) {
        File buildFile = projectDir.newFile('build.gradle')

        writeBuildFileContents(
                buildFile,
                config
        )

        return buildFile
    }

    static void writeBuildFileContents(
            File buildFile,
            ProjectBuildFileConfig config = new ProjectBuildFileConfig()
    ) {
        buildFile << """
            buildscript {
                repositories {
                    mavenCentral()
                }
            }

            plugins {
                id 'groovy'
                ${config.includeProjektorPlugin ? "id 'dev.projektor.publish'" : ""}
                ${config.includeJacocoPlugin ? "id 'jacoco'" : ""}
                ${config.includeKoverPlugin ? "id 'org.jetbrains.kotlinx.kover' version '${config.koverPluginVersion}'" : ""}
                ${config.includeCodeNarcPlugin ? "id 'codenarc'" : ""}
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                implementation('org.codehaus.groovy:groovy-all:2.5.13')

                testImplementation('org.spockframework:spock-core:1.3-groovy-2.5')
            }

            ${config.includeJacocoPlugin ? "jacocoTestReport { dependsOn test }": ""}
            
            ${config.includeKoverPlugin ? "test { kover { enabled = true } }": ""}
            ${config.includeKoverPlugin ? "kover { coverageEngine.set(kotlinx.kover.api.CoverageEngine.JACOCO) }": ""}

            ${config.includeCodeNarcPlugin ? "codenarc { reportFormat 'text' }": ""}
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
