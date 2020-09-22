package projektor.parser

import org.apache.commons.lang3.RandomStringUtils
import projektor.parser.model.TestCase
import projektor.parser.model.TestSuite
import spock.lang.Specification
import spock.lang.Subject

class JUnitResultsParserLargeSpec extends Specification {
    @Subject
    JUnitResultsParser testResultsParser = new JUnitResultsParser()

    def "should be able to parse results with large XML attributes"() {
        given:
        String resultsXmlWithHugeAttribute = """<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="projektor.example.spock.PassingSpec" tests="1" skipped="0" failures="0" errors="0" timestamp="2019-10-01T13:15:45" hostname="Craigs-MacBook-Pro.local" time="0.002">
  <properties/>
  <testcase name="long-name-${RandomStringUtils.randomAlphabetic(1_000_000)}" classname="projektor.example.spock.PassingSpec" time="0.002"/>
  <system-out><![CDATA[]]></system-out>
  <system-err><![CDATA[]]></system-err>
</testsuite>
"""

        when:
        TestSuite testSuite = testResultsParser.parseTestSuite(resultsXmlWithHugeAttribute)

        then:
        testSuite.testCases.size() == 1

        TestCase testCase = testSuite.testCases[0]

        testCase.name.startsWith("long-name-")
    }
}
