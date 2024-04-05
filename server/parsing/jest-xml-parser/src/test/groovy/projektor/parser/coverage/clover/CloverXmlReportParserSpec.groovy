package projektor.parser.coverage.clover

import projektor.parser.coverage.clover.model.Coverage
import projektor.server.example.coverage.JacocoXmlLoader
import projektor.server.example.coverage.CloverXmlLoader
import spock.lang.Specification
import spock.lang.Unroll

class CloverXmlReportParserSpec extends Specification {
    def "should parse Jest XML report"() {
        given:
        String reportXml = new CloverXmlLoader().uiClover()

        when:
        Coverage report = new CloverXmlReportParser().parseReport(reportXml)

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

    def "should parse Jest coverage XML report without a package"()  {
        given:
        String reportXml = new CloverXmlLoader().noPackage()

        when:
        Coverage report = new CloverXmlReportParser().parseReport(reportXml)

        then:
        report.project != null
        report.project.metrics != null

        report.project.packages == null

        report.project.name == "All files"

        // <metrics statements="44" coveredstatements="44" conditionals="24" coveredconditionals="24" methods="2" coveredmethods="2"/>

        report.project.metrics.statements == 44
        report.project.metrics.coveredStatements == 44

        report.project.metrics.conditionals == 24
        report.project.metrics.coveredConditionals == 24
    }

    @Unroll
    def "should determine if report is Jest report #shouldBeJest"() {
        expect:
        CloverXmlReportParser.isCloverReport(reportXml) == shouldBeJest

        where:
        reportXml                               || shouldBeJest
        new CloverXmlLoader().uiClover()        || true
        new JacocoXmlLoader().jacocoXmlParser() || false
    }
}
