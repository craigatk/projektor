package projektor.parser.jest

import projektor.parser.jest.model.Coverage
import projektor.server.example.coverage.JacocoXmlLoader
import projektor.server.example.coverage.JestXmlLoader
import spock.lang.Specification
import spock.lang.Unroll

class JestXmlReportParserSpec extends Specification {
    def "should parse Jest XML report"() {
        given:
        String reportXml = new JestXmlLoader().ui()

        when:
        Coverage report = new JestXmlReportParser().parseReport(reportXml)

        then:
        report.project != null
        report.project.metrics != null

        report.project.name == "All files"

        // <metrics statements="1021" coveredstatements="924" conditionals="195" coveredconditionals="158" methods="196" coveredmethods="154" elements="1412" coveredelements="1236" complexity="0" loc="1021" ncloc="1021" packages="22" files="75" classes="75"/>

        report.project.metrics.statements == 1021
        report.project.metrics.coveredStatements == 924

        report.project.metrics.conditionals == 195
        report.project.metrics.coveredConditionals == 158
    }

    @Unroll
    def "should determine if report is Jest report #shouldBeJest"() {
        expect:
        JestXmlReportParser.isJestReport(reportXml) == shouldBeJest

        where:
        reportXml                               || shouldBeJest
        new JestXmlLoader().ui()                || true
        new JacocoXmlLoader().jacocoXmlParser() || false
    }
}
