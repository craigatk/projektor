package projektor.plugin.functionaltest

import org.junit.Rule
import org.junit.rules.TemporaryFolder

class SingleProjectFunctionalSpecification extends ProjektorPluginFunctionalSpecification {
    @Rule
    TemporaryFolder projectRootDir = new TemporaryFolder()

    File buildFile

    def setup() {
        buildFile = projectRootDir.newFile('build.gradle')
        buildFile << """
            buildscript {
                repositories {
                    jcenter()
                }
            }

            plugins {
                id 'groovy'
                id 'dev.projektor.publish'
            }
            
            repositories {
                jcenter()
            }
            
            dependencies {
                testImplementation('org.spockframework:spock-core:1.3-groovy-2.5')
            }
        """.stripIndent()
    }
}
