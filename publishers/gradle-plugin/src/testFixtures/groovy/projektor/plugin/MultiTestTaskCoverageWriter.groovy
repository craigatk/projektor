package projektor.plugin

class MultiTestTaskCoverageWriter {
    static File writeSourceCodeFile(File sourceDirectory) {
        File sourceFile = new File(sourceDirectory, "Example.groovy")

        sourceFile << """package projektor

class Example {
    String foo(boolean first) {
        if (first) { // Covered
            return "A" // Covered
        }

        return "B" // Covered
    }

    String bar(boolean first) {
        if (first) { // Partial
            return "C" // Covered
        }

        return "D" // Missed
    }

    String baz(boolean first) {
        if (first) { // Partial
            return "E" // Covered
        }

        return "F" // Missed
    }
}
"""
        return sourceFile
    }

    static File writeFirstPartialCoverageSpecFile(File testDirectory, String specClassName) {
        File specFile = new File(testDirectory, "${specClassName}.groovy")

        specFile << """package projektor

import spock.lang.Specification

class $specClassName extends Specification {
    def "first foo should be A"() {
        expect:
        new Example().foo(true) == "A"
    }

    def "first bar should be C"() {
        expect:
        new Example().bar(true) == "C"
    }
}
"""
        return specFile
    }

    static File writeSecondPartialCoverageSpecFile(File testDirectory, String specClassName) {
        File specFile = new File(testDirectory, "${specClassName}.groovy")

        specFile << """package projektor

import spock.lang.Specification

class $specClassName extends Specification {
    def "first foo should be A"() {
        expect:
        new Example().foo(true) == "A"
    }

    def "second foo should be B"() {
        expect:
        new Example().foo(false) == "B"
    }

    def "first baz should be E"() {
        expect:
        new Example().baz(true) == "E"
    }
}
"""

        return specFile
    }
}
