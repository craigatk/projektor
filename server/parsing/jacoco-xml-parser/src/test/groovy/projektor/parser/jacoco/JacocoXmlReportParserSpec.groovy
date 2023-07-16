package projektor.parser.jacoco

import projektor.parser.jacoco.model.Counter
import projektor.parser.jacoco.model.CounterType
import projektor.parser.jacoco.model.Report
import projektor.server.example.coverage.CloverXmlLoader
import projektor.server.example.coverage.JacocoXmlLoader
import spock.lang.Specification
import spock.lang.Unroll

class JacocoXmlReportParserSpec extends Specification {
    def "should parse top-level coverage stats"() {
        given:
        String reportXml = new JacocoXmlLoader().serverApp()

        when:
        Report report = new JacocoXmlReportParser().parseReport(reportXml)

        then:
        report.name == "server-app"

        /* Expected values:
        <counter type="INSTRUCTION" missed="335" covered="8816"/>
        <counter type="BRANCH" missed="57" covered="191"/>
        <counter type="LINE" missed="25" covered="953"/>
        <counter type="COMPLEXITY" missed="60" covered="373"/>
        <counter type="METHOD" missed="6" covered="301"/>
        <counter type="CLASS" missed="2" covered="173"/>
        */

        report.counters.size() == 6
        verifyCounter(report.counters, CounterType.INSTRUCTION, 335, 8816)
        verifyCounter(report.counters, CounterType.BRANCH, 57, 191)
        verifyCounter(report.counters, CounterType.LINE, 25, 953)
        verifyCounter(report.counters, CounterType.COMPLEXITY, 60, 373)
        verifyCounter(report.counters, CounterType.METHOD, 6, 301)
        verifyCounter(report.counters, CounterType.CLASS, 2, 173)
    }

    @Unroll
    def "should determine if report is Jacoco or not #shouldBeJacoco"() {
        expect:
        JacocoXmlReportParser.isJacocoReport(reportXml) == shouldBeJacoco

        where:
        reportXml                               || shouldBeJacoco
        new JacocoXmlLoader().serverApp()       || true
        new JacocoXmlLoader().jacocoXmlParser() || true
        new CloverXmlLoader().uiClover() || false
    }

    private static void verifyCounter(List<Counter> counters, CounterType type, int expectedMissed, int expectedCovered) {
        assert counters.find { it.type == type }

        Counter counter = counters.find { it.type == type }

        assert counter.missed == expectedMissed
        assert counter.covered == expectedCovered
    }
}
