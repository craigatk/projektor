package projektor.plugin

class CodeUnderTestWriter {
    static void writeSourceCodeFile(File sourceDirectory) {
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
}
