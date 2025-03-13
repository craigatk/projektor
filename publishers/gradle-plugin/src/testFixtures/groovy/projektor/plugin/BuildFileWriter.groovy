package projektor.plugin

import org.junit.rules.TemporaryFolder

class BuildFileWriter {
    static File createProjectBuildFile(
            TemporaryFolder projectDir,
            ProjectBuildFileConfig config = new ProjectBuildFileConfig()
    ) {
        File buildFile = projectDir.newFile('build.gradle')

        if (config.includeKoverPlugin) {
            writeKotlinBuildFileContents(buildFile, config)
        } else {
            writeBuildFileContents(buildFile, config)
        }

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
                ${config.includeCodeNarcPlugin ? "id 'codenarc'" : ""}
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                implementation('org.codehaus.groovy:groovy-all:3.0.21')

                testImplementation('org.spockframework:spock-core:2.4-M5-groovy-3.0')
            }

            test {
                useJUnitPlatform()
            }

            ${config.includeJacocoPlugin ? "jacocoTestReport { dependsOn test }": ""}

            ${config.includeCodeNarcPlugin ? "codenarc { reportFormat 'text' }": ""}
        """.stripIndent()
    }

    static void writeKotlinBuildFileContents(
            File buildFile,
            ProjectBuildFileConfig config = new ProjectBuildFileConfig()
    ) {
        String koverCoverageEngine = ""

        if (config.includeKoverPlugin) {
            if (config.koverPluginVersion.contains("0.7")) {
                koverCoverageEngine = "kover { useJacoco() }"
            } else {
                koverCoverageEngine = "kover { useJacoco(\"0.8.12\") }"
            }
        }

        String koverConfigSetup = ""

        if (config.includeKoverPlugin) {
            if (config.koverPluginVersion.contains("0.7")) {
                koverConfigSetup = """tasks.withType(kotlinx.kover.gradle.plugin.tasks.reports.KoverXmlTask) {
                    dependsOn(test)
                }"""
            } else {
                koverConfigSetup = "test { kover { enabled = true } }"
            }
        }

        buildFile << """
            buildscript {
                repositories {
                    mavenCentral()
                }

                dependencies {
                    classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.10"
                }
            }

            plugins {
                ${config.includeProjektorPlugin ? "id 'dev.projektor.publish'" : ""}
                ${config.includeKoverPlugin ? "id 'org.jetbrains.kotlinx.kover' version '${config.koverPluginVersion}'" : ""}
            }

            apply plugin: "kotlin"
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                implementation "org.jetbrains.kotlin:kotlin-stdlib:2.1.10"
                implementation "org.jetbrains.kotlin:kotlin-reflect:2.1.10"
            
                testImplementation "io.kotest:kotest-runner-junit5-jvm:5.9.1"
                testImplementation "io.strikt:strikt-core:0.34.1"
            }
            
            ${koverConfigSetup}
            ${koverCoverageEngine}

            test {
                useJUnitPlatform()
            }
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
