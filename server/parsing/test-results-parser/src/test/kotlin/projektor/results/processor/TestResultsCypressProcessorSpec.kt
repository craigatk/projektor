package projektor.results.processor

import io.kotest.core.spec.style.StringSpec
import projektor.parser.ResultsXmlLoader
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

class TestResultsCypressProcessorSpec : StringSpec({
    "should parse single Cypress test result with file name in separate suite" {
        val testResultsProcessor = TestResultsProcessor()
        val resultsXmlLoader = ResultsXmlLoader()
        val blob = resultsXmlLoader.cypressAttachmentsSpecWithFilePath()

        val testSuiteList = testResultsProcessor.parseResultsBlob(blob)
        expectThat(testSuiteList).hasSize(2).any {
            get { file }.isEqualTo("cypress\\integration\\attachments.spec.js")
            get { name }.isEqualTo("test run with attachments")
            get { testCases }.isNotNull().hasSize(2)
        }
    }

    "should parse multiple Cypress test results with file name in separate suite" {
        val testResultsProcessor = TestResultsProcessor()
        val resultsXmlLoader = ResultsXmlLoader()
        val blob =
            resultsXmlLoader.cypressAttachmentsSpecWithFilePath() +
                resultsXmlLoader.cypressRepositoryTimelineSpecWithFilePath()

        val testSuiteList = testResultsProcessor.parseResultsBlob(blob)
        expectThat(testSuiteList).hasSize(4).any {
            get { file }.isEqualTo("cypress\\integration\\attachments.spec.js")
            get { name }.isEqualTo("test run with attachments")
            get { testCases }.isNotNull().hasSize(2)
        }.any {
            get { file }.isEqualTo("cypress\\integration\\repository_timeline.spec.js")
            get { name }.isEqualTo("repository coverage")
            get { testCases }.isNotNull().hasSize(2)
        }
    }

    "should parse single Cypress test result with file name and test suite name in separate suite" {
        val testResultsProcessor = TestResultsProcessor()
        val resultsXmlLoader = ResultsXmlLoader()
        val blob = resultsXmlLoader.cypressWithFilePathAndRootSuiteNameSet()

        val testSuiteList = testResultsProcessor.parseResultsBlob(blob)
        expectThat(testSuiteList).hasSize(2).any {
            get { file }.isEqualTo("cypress\\integration\\repository_timeline.spec.js")
            get { name }.isEqualTo("repository coverage suite")
            get { testCases }.isNotNull().hasSize(2)
        }
    }

    "should filter out empty Cypress test suites" {
        val testResultsProcessor = TestResultsProcessor()
        val resultsXmlLoader = ResultsXmlLoader()
        val blob =
            resultsXmlLoader.cypressEmptyTestSuites() +
                resultsXmlLoader.cypressRepositoryTimelineSpecWithFilePath()

        val testSuiteList = testResultsProcessor.parseResultsBlob(blob)
        expectThat(testSuiteList).hasSize(2).any {
            get { file }.isEqualTo("cypress\\integration\\repository_timeline.spec.js")
            get { name }.isEqualTo("repository coverage")
            get { testCases }.isNotNull().hasSize(2)
        }
    }

    "when only one empty test suite should return empty list" {
        val testResultsProcessor = TestResultsProcessor()
        val resultsXmlLoader = ResultsXmlLoader()
        val blob = resultsXmlLoader.cypressEmptyTestSuites()

        val testSuiteList = testResultsProcessor.parseResultsBlob(blob)
        expectThat(testSuiteList).hasSize(0)
    }
})
