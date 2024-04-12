package projektor.results.processor

import io.kotest.core.spec.style.StringSpec
import projektor.parser.ResultsXmlLoader
import projektor.parser.model.TestSuite
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.map

class TestResultsProcessorSpec : StringSpec({

    "should parse blob with multiple test suites elements into a test suite list" {
        val testResultsProcessor = TestResultsProcessor()
        val resultsXmlLoader = ResultsXmlLoader()
        val blob = resultsXmlLoader.cypressResults().joinToString("\n")

        val testSuiteList = testResultsProcessor.parseResultsBlob(blob)

        expectThat(testSuiteList)
            .hasSize(12)
            .map(TestSuite::name)
            .contains(
                "test suite",
                "test run with failed test cases",
            )
    }

    "should parse blob with multiple test suite elements into a test suite list" {
        val testResultsProcessor = TestResultsProcessor()
        val resultsXmlLoader = ResultsXmlLoader()
        val blob = listOf(resultsXmlLoader.passing(), resultsXmlLoader.failing()).joinToString("\n")

        val testSuiteList = testResultsProcessor.parseResultsBlob(blob)

        expectThat(testSuiteList)
            .hasSize(2)
            .map(TestSuite::name)
            .contains(
                "projektor.example.spock.PassingSpec",
                "projektor.example.spock.FailingSpec",
            )
    }

    "should parse blob with single test suite into test suite list" {
        val testResultsProcessor = TestResultsProcessor()
        val resultsXmlLoader = ResultsXmlLoader()
        val blob = resultsXmlLoader.passing()

        val testSuiteList = testResultsProcessor.parseResultsBlob(blob)

        expectThat(testSuiteList)
            .hasSize(1)
            .map(TestSuite::name)
            .contains(
                "projektor.example.spock.PassingSpec",
            )
    }

    "should parse pytest results with lower-case utf-8 encoding in xml header" {
        val testResultsProcessor = TestResultsProcessor()
        val resultsXmlLoader = ResultsXmlLoader()
        val blob = resultsXmlLoader.pytestPassing()

        val testSuiteList = testResultsProcessor.parseResultsBlob(blob)

        expectThat(testSuiteList)
            .hasSize(1)
            .map(TestSuite::name)
            .contains(
                "pytest",
            )
    }

    "should parse null blob into empty list" {
        val testResultsProcessor = TestResultsProcessor()
        val blob: String? = null

        val testSuiteList = testResultsProcessor.parseResultsBlob(blob)

        expectThat(testSuiteList)
            .hasSize(0)
    }

    "should parse empty blob into empty list" {
        val testResultsProcessor = TestResultsProcessor()
        val blob = ""

        val testSuiteList = testResultsProcessor.parseResultsBlob(blob)

        expectThat(testSuiteList)
            .hasSize(0)
    }
})
