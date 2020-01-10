package projektor.parser

import projektor.parser.model.TestSuite
import spock.lang.Specification
import spock.lang.Subject

class JUnitResultsParserBlogSpec extends Specification {
    @Subject
    JUnitResultsParser testResultsParser = new JUnitResultsParser()

    ResultsXmlLoader resultsXmlLoader = new ResultsXmlLoader()

    void "should parse blob with multiple test suites elements into a test suite list"() {
        given:
        String blob = resultsXmlLoader.cypressResults().join("\n")

        when:
        List<TestSuite> testSuiteList = testResultsParser.parseResultsBlob(blob)

        then:
        testSuiteList.size() == 12

        testSuiteList.find { it.name == "test suite" }
        testSuiteList.find { it.name == "test run with failed test cases" }
    }

    void "should parse blob with multiple test suite elements into a test suite list"() {
        given:
        String blob = [resultsXmlLoader.passing(), resultsXmlLoader.failing()].join("\n")

        when:
        List<TestSuite> testSuiteList = testResultsParser.parseResultsBlob(blob)

        then:
        testSuiteList.size() == 2

        testSuiteList.find { it.name == 'projektor.example.spock.PassingSpec' }
        testSuiteList.find { it.name == 'projektor.example.spock.FailingSpec' }
    }

    void 'should parse blob with single test suite into test suite list'() {
        given:
        String blob = resultsXmlLoader.passing()

        when:
        List<TestSuite> testSuiteList = testResultsParser.parseResultsBlob(blob)

        then:
        testSuiteList.size() == 1

        testSuiteList.find { it.name == 'projektor.example.spock.PassingSpec' }
    }
}
