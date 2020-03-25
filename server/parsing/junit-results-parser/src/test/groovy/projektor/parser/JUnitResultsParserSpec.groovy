package projektor.parser

import projektor.parser.model.TestCase
import projektor.parser.model.TestSuite
import spock.lang.Specification
import spock.lang.Subject

class JUnitResultsParserSpec extends Specification {

    @Subject
    JUnitResultsParser testResultsParser = new JUnitResultsParser()

    ResultsXmlLoader resultsXmlLoader = new ResultsXmlLoader()

    void "should parse passing spec"() {
        given:
        String testResultsStr = resultsXmlLoader.passing()

        when:
        TestSuite testSuite = testResultsParser.parseTestSuite(testResultsStr)

        then:
        testSuite.name == 'projektor.example.spock.PassingSpec'
        testSuite.tests == 1

        and:
        testSuite.testCases.size() == 1
        testSuite.testCases[0].name == "should pass"
        testSuite.testCases[0].className == "projektor.example.spock.PassingSpec"
        testSuite.testCases[0].time == 0.002
    }

    void "should parse failing test with output"() {
        given:
        String testResultsStr = resultsXmlLoader.failing()

        when:
        TestSuite testSuite = testResultsParser.parseTestSuite(testResultsStr)

        then:
        testSuite.name == 'projektor.example.spock.FailingSpec'
        testSuite.tests == 2

        and:
        testSuite.testCases.size() == 2

        TestCase shouldFail = testSuite.testCases.find { it.name == 'should fail' }
        shouldFail.className == 'projektor.example.spock.FailingSpec'
        shouldFail.time == 0.119

        TestCase shouldFailWithOutput = testSuite.testCases.find { it.name == 'should fail with output' }
        shouldFailWithOutput.className == 'projektor.example.spock.FailingSpec'

        and:
        shouldFailWithOutput.failure != null

        List<String> failureMessageLines = shouldFailWithOutput.failure.message.stripMargin().readLines().findAll { it }
        failureMessageLines == ['Condition not satisfied:', 'actual == 3', '      |', '1      false']

        shouldFailWithOutput.failure.text != null
        shouldFailWithOutput.failure.text.contains('at projektor.example.spock.FailingSpec.should fail with output(FailingSpec.groovy:22)')

        and:
        testSuite.systemOut == """A line in the given block
Another line in the given block
A line in the when block
A line in the then block
"""
    }

    void "should parse results with system out and system err"() {
        given:
        String testResultsStr = resultsXmlLoader.output()

        when:
        TestSuite testSuite = testResultsParser.parseTestSuite(testResultsStr)

        then:
        testSuite.systemOut != null
        int systemOutLineCount = testSuite.systemOut.readLines().size()
        systemOutLineCount == 100

        and:
        testSuite.systemErr != null
        int systemErrLineCount = testSuite.systemErr.readLines().size()
        systemErrLineCount == 200
    }

    void 'should parse whether test case is ignored'() {
        when:
        TestSuite testSuite = testResultsParser.parseTestSuite(resultsXmlLoader.someIgnored())

        then:
        List<TestCase> testCases = testSuite.testCases
        testCases.size() == 10

        !testCases.find { it.name == 'should run test case 1' }.skipped
        !testCases.find { it.name == 'should run test case 2' }.skipped
        testCases.find { it.name == 'should not run test case 3' }.skipped
        !testCases.find { it.name == 'should run test case 4' }.skipped
        !testCases.find { it.name == 'should run test case 5' }.skipped
        testCases.find { it.name == 'should not run test case 6' }.skipped
        testCases.find { it.name == 'should not run test case 7' }.skipped
        !testCases.find { it.name == 'should run test case 8' }.skipped
        !testCases.find { it.name == 'should run test case 9' }.skipped
        !testCases.find { it.name == 'should run test case 10' }.skipped
    }

    void 'should parse multiple results from wrapped test suites XML'() {
        given:
        String testSuitesXml =
                [resultsXmlLoader.passing(),
                 resultsXmlLoader.failing(),
                 resultsXmlLoader.output()]
                        .join("")
        .replace("""<?xml version="1.0" encoding="UTF-8"?>""", "")

        String resultsWrappedInTestSuitesXML = """<?xml version="1.0" encoding="UTF-8"?>
            <testsuites>
            ${testSuitesXml}
            </testsuites>
            """.stripIndent()

        when:
        List<TestSuite> testSuites = testResultsParser.parseTestSuites(resultsWrappedInTestSuitesXML)

        then:
        testSuites.size() == 3

        testSuites.name.contains('projektor.example.spock.PassingSpec')
        testSuites.name.contains('projektor.example.spock.FailingSpec')
        testSuites.name.contains('projektor.example.spock.OutputSpec')
    }
}
