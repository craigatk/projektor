package projektor.plugin.testkit

abstract class SingleProjectSpec extends ProjectSpec {
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
                id 'projektor.publish'
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
