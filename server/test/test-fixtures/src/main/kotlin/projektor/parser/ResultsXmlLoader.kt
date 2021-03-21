package projektor.parser

import io.github.classgraph.ClassGraph

class ResultsXmlLoader {
    fun passing() = loadTextFromFile("TEST-projektor.example.spock.PassingSpec.xml")

    fun failing() = loadTextFromFile("TEST-projektor.example.spock.FailingSpec.xml")
    fun failingAnother() = loadTextFromFile("TEST-projektor.example.spock.AnotherFailingSpec.xml")

    fun failingLongFailureMessage() = loadTextFromFile("TEST-projektor.testsuite.GetTestSuiteApplicationTest-long-failure-output.xml")

    fun longOutput() = loadTextFromFile("TEST-projektor.example.spock.LongOutputSpec.xml")

    fun reallyLongOutput() = loadTextFromFile("TEST-projektor.example.spock.ReallyLongOutputSpec.xml")

    fun reallyLongOutput5000() = loadTextFromFile("TEST-projektor.example.spock.ReallyLongOutput5000Spec.xml")

    fun reallyLongOutput10000() = loadTextFromFile("TEST-projektor.example.spock.ReallyLongOutput10000Spec.xml")

    fun output() = loadTextFromFile("TEST-projektor.example.spock.OutputSpec.xml")

    fun someIgnored() = loadTextFromFile("TEST-projektor.example.spock.IgnoreSomeMethodsSpec.xml")

    fun someIgnoredSomeFailing() = loadTextFromFile("TEST-projektor.example.spock.IgnoreSomeMethodsAndSomeFailingSpec.xml")

    fun slow() = loadTextFromFile("TEST-projektor.example.spock.SlowSpec.xml")

    fun slower() = loadTextFromFile("TEST-projektor.example.spock.SlowerSpec.xml")

    fun invalid() = loadTextFromFile("TEST-projektor.example.spock.PassingSpec.xml")
        .replace("<testsuite", "testsuite")

    fun gradleSingleTestCaseSystemOutFail() = loadTextFromFile("gradle-single-test-case-system-out-fail.xml")
    fun gradleSingleTestCaseSystemOutPass() = loadTextFromFile("gradle-single-test-case-system-out-pass.xml")

    fun cutOffResultsGradle() = loadTextFromFile("cut-off-results-gradle.xml")

    fun cypressResults(): List<String> {
        val cypressResourceList = ClassGraph()
            .whitelistPaths("cypress")
            .scan()
            .getResourcesWithExtension("xml")

        return cypressResourceList
            .map { String(it.load()) }
    }

    fun cypressResultsWithFilePaths(): List<String> {
        val cypressResourceList = ClassGraph()
            .whitelistPaths("cypress-file-path")
            .scan()
            .getResourcesWithExtension("xml")

        return cypressResourceList
            .map { String(it.load()) }
    }

    fun cypressEmptyTestSuites() = loadTextFromFile("cypress/cypress-empty-test-suites.xml")

    fun cypressAttachmentsSpecWithFilePath() = loadTextFromFile("cypress-file-path/cypress-attachments.xml")
    fun cypressRepositoryTimelineSpecWithFilePath() = loadTextFromFile("cypress-file-path/cypress-repository-timeline.xml")
    fun cypressWithFilePathAndRootSuiteNameSet() = loadTextFromFile("cypress-file-path/cypress-root-suite-name.xml")

    fun cypressFailingDashboardTestWithScreenshots() = loadTextFromFile("cypress-file-path/cypress-failing-dashboard-test-with-screenshots.xml")
    fun cypressFailingAttachmentsTestWithScreenshot() = loadTextFromFile("cypress-file-path/cypress-failing-attachments-test-with-screenshot.xml")

    fun jestUi() = loadTextFromFile("jest/ui-junit.xml")

    fun jestUiFilePath() = loadTextFromFile("jest/ui-file-path-junit.xml")

    fun pytestFailing() = loadTextFromFile("pytest/pytest-failing.xml")

    fun pytestPassing() = loadTextFromFile("pytest/pytest-passing.xml")

    fun k6Example() = loadTextFromFile("k6/k6-junit.xml")
    fun k6GetFailedTestCasesLarge() = loadTextFromFile("k6/k6-getFailedTestCasesLarge.xml")

    fun singleQuoteXmlDeclaration() = loadTextFromFile("single-quote-xml-declaration.xml")

    private fun loadTextFromFile(filename: String) = javaClass
        .getResourceAsStream("/$filename")
        .bufferedReader()
        .readText()
}
