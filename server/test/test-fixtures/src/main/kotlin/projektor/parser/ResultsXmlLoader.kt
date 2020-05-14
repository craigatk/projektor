package projektor.parser

import io.github.classgraph.ClassGraph
import projektor.results.processor.ResultsXmlMerger

class ResultsXmlLoader {
    fun passing() = loadTextFromFile("TEST-projektor.example.spock.PassingSpec.xml")

    fun failing() = loadTextFromFile("TEST-projektor.example.spock.FailingSpec.xml")

    fun failingLongFailureMessage() = loadTextFromFile("TEST-projektor.testsuite.GetTestSuiteApplicationTest-long-failure-output.xml")

    fun longOutput() = loadTextFromFile("TEST-projektor.example.spock.LongOutputSpec.xml")

    fun reallyLongOutput() = loadTextFromFile("TEST-projektor.example.spock.ReallyLongOutputSpec.xml")

    fun reallyLongOutput5000() = loadTextFromFile("TEST-projektor.example.spock.ReallyLongOutput5000Spec.xml")

    fun reallyLongOutput10000() = loadTextFromFile("TEST-projektor.example.spock.ReallyLongOutput10000Spec.xml")

    fun output() = loadTextFromFile("TEST-projektor.example.spock.OutputSpec.xml")

    fun someIgnored() = loadTextFromFile("TEST-projektor.example.spock.IgnoreSomeMethodsSpec.xml")

    fun someIgnoredSomeFailing() = loadTextFromFile("TEST-projektor.example.spock.IgnoreSomeMethodsAndSomeFailingSpec.xml")

    fun invalid() = loadTextFromFile("TEST-projektor.example.spock.PassingSpec.xml")
            .replace("<testsuite", "testsuite")

    fun cypressResults(): List<String> {
        val cypressResourceList = ClassGraph()
                .whitelistPaths("cypress")
                .scan()
                .getResourcesWithExtension("xml")

        return cypressResourceList
                .map { String(it.load()) }
                .map(ResultsXmlMerger::removeTestSuitesWrapper)
    }

    private fun loadTextFromFile(filename: String) = javaClass
            .getResourceAsStream("/$filename")
            .bufferedReader()
            .readText()
}
