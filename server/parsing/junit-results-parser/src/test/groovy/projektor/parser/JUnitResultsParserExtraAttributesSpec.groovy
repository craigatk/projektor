package projektor.parser

import projektor.parser.model.TestCase
import projektor.parser.model.TestSuite
import spock.lang.Specification
import spock.lang.Subject

class JUnitResultsParserExtraAttributesSpec extends Specification {
    @Subject
    JUnitResultsParser testResultsParser = new JUnitResultsParser()

    def "should be able to parse results with extra XML attributes"() {
        given:
        String resultsXmlWithHugeAttribute = """<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="projektor.example.spock.SkippedWithExtraAttributesSpec" tests="1" skipped="1" failures="0" errors="0" timestamp="2019-10-01T13:15:45" hostname="Craigs-MacBook-Pro.local" time="0.002">
  <properties/>
  <testcase name="with-extra-attributes" classname="projektor.example.spock.SkippedWithExtraAttributesSpec" time="0.002">
    <skipped message="skipped-for-reasons"/>
  </testcase>
</testsuite>
"""

        when:
        TestSuite testSuite = testResultsParser.parseTestSuite(resultsXmlWithHugeAttribute)

        then:
        testSuite.testCases.size() == 1

        TestCase testCase = testSuite.testCases[0]

        testCase.skipped != null
        testCase.name == "with-extra-attributes"
    }
}
