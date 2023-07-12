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
                implementation('org.codehaus.groovy:groovy-all:2.5.13')

                testImplementation('org.spockframework:spock-core:1.3-groovy-2.5')
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
            }  else if (config.koverPluginVersion.contains("0.6")) {
                // https://github.com/Kotlin/kotlinx-kover/blob/v0.6.0/docs/migration-to-0.6.0.md#properties-coverageengine-intellijengineversion-and-jacocoengineversion-were-removed
                koverCoverageEngine = "kover { engine = kotlinx.kover.api.DefaultJacocoEngine.INSTANCE }"
            } else {
                koverCoverageEngine = "kover { coverageEngine.set(kotlinx.kover.api.CoverageEngine.JACOCO) }"
            }
        }

        String koverConfigSetup = ""

        if (config.includeKoverPlugin) {
            if (config.koverPluginVersion.contains("0.7")) {
                koverConfigSetup = """tasks.withType(kotlinx.kover.gradle.plugin.tasks.reports.KoverXmlTask) {
                    dependsOn(test)
                }"""
            } else if (config.koverPluginVersion.contains("0.6")) {
                koverConfigSetup = """tasks.withType(kotlinx.kover.tasks.KoverXmlTask) {
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
                    classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.22"
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
                implementation "org.jetbrains.kotlin:kotlin-stdlib:1.8.22"
                implementation "org.jetbrains.kotlin:kotlin-reflect:1.8.22"
            
                testImplementation "io.kotest:kotest-runner-junit5-jvm:5.6.2"
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
