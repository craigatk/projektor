package projektor.parser

import projektor.parser.model.TestCase
import projektor.parser.model.TestSuite
import spock.lang.Specification
import spock.lang.Subject

class JUnitResultsParserTestCaseOutputSpec extends Specification {
    @Subject
    JUnitResultsParser resultsParser = new JUnitResultsParser()

    ResultsXmlLoader resultsXmlLoader = new ResultsXmlLoader()

    def "should parse system out and err at the test case level"() {
        when:
        TestSuite testSuite = resultsParser.parseTestSuite(resultsXmlLoader.gradleSingleTestCaseSystemOutFail())

        then:
        testSuite.testCases.size() == 1

        TestCase testCase = testSuite.testCases[0]
        testCase.name == "should save compressed grouped test results()"

        testCase.systemOut.contains("HikariPool-1 - Starting")
        testCase.systemErr.contains("System error")
    }
}
