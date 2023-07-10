package projektor.plugin

class CodeUnderTestWriter {
    static File writeSourceCodeFile(File sourceDirectory) {
        File sourceFile = new File(sourceDirectory, "MyClass.groovy")

        sourceFile << """package projektor

class MyClass {
    static String foo() {
      return "bar"
    }
    
    static String boo() {
      return "baz"
    }
}
"""

        return sourceFile
    }

    static File writeKotlinSourceCodeFile(File sourceDirectory) {
        File sourceFile = new File(sourceDirectory, "Foo.kt")

        sourceFile << """package projektor
fun foo(): String {
    return "bar"
}

fun baz(): String {
    return "foo"
}
"""

        return sourceFile
    }

    static void writePartialCoverageSpecFile(File testDirectory, String specClassName) {
        File specFile = new File(testDirectory, "${specClassName}.groovy")

        specFile << """package projektor

import spock.lang.Specification

class ${specClassName} extends Specification {
    void "sample test"() {
        expect:
        MyClass.foo() == "bar"
    }
}
"""
    }

    static void writePartialCoverageKotestFile(File testDirectory, String testClassName) {
        File specFile = new File(testDirectory, "${testClassName}.kt")

        specFile << """package projektor

import io.kotest.core.spec.style.StringSpec
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class $testClassName : StringSpec() {
    init {
        "should return bar" {
            expectThat(foo()).isEqualTo("bar")
        }
    }
}
"""
    }

    static void writeSecondPartialCoverageSpecFile(File testDirectory, String specClassName) {
        File specFile = new File(testDirectory, "${specClassName}.groovy")

        specFile << """package projektor

import spock.lang.Specification

class ${specClassName} extends Specification {
    void "sample test"() {
        expect:
        MyClass.boo() == "baz"
    }
}
"""
    }

    static void writeFullCoverageSpecFile(File testDirectory, String specClassName) {
        File specFile = new File(testDirectory, "${specClassName}.groovy")

        specFile << """package projektor

import spock.lang.Specification

class ${specClassName} extends Specification {
    void "sample test"() {
        expect:
        MyClass.foo() == "bar"
        MyClass.boo() == "baz"
    }
}
"""
    }

    static File writeResourcesFile(File resourcesDirectory) {
        File versionFile = new File(resourcesDirectory, "version.txt")

        versionFile << "1.0"

        return versionFile
    }
}
