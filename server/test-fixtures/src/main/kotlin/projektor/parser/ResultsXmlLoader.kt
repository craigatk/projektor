package projektor.parser

import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import projektor.results.processor.ResultsXmlMerger

class ResultsXmlLoader {
    fun passing() = loadTextFromFile("TEST-projektor.example.spock.PassingSpec.xml")

    fun failing() = loadTextFromFile("TEST-projektor.example.spock.FailingSpec.xml")

    fun failingLongFailureMessage() = loadTextFromFile("TEST-projektor.testsuite.GetTestSuiteApplicationTest-long-failure-output.xml")

    fun longOutput() = loadTextFromFile("TEST-projektor.example.spock.LongOutputSpec.xml")

    fun output() = loadTextFromFile("TEST-projektor.example.spock.OutputSpec.xml")

    fun someIgnored() = loadTextFromFile("TEST-projektor.example.spock.IgnoreSomeMethodsSpec.xml")

    fun someIgnoredSomeFailing() = loadTextFromFile("TEST-projektor.example.spock.IgnoreSomeMethodsAndSomeFailingSpec.xml")

    fun cypressResults(): List<String> {
        val reflections = Reflections(null, ResourcesScanner())
        val resourceList = reflections.getResources { x -> x != null && x.contains("cypress") && x.contains("xml") }

        return resourceList.map(::loadTextFromFile).map(ResultsXmlMerger::removeTestSuitesWrapper)
    }

    private fun loadTextFromFile(filename: String) = javaClass
            .getResourceAsStream("/$filename")
            .bufferedReader()
            .readText()
}
