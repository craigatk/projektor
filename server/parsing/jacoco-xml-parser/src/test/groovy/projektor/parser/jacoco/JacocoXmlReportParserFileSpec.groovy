package projektor.parser.jacoco

import projektor.parser.jacoco.model.Counter
import projektor.parser.jacoco.model.CounterType
import projektor.parser.jacoco.model.LineType
import projektor.parser.jacoco.model.Report
import projektor.parser.jacoco.model.ReportPackage
import projektor.parser.jacoco.model.SourceFile
import projektor.server.example.coverage.JacocoXmlLoader
import spock.lang.Specification

class JacocoXmlReportParserFileSpec extends Specification {
    def "should parse file-level stats from Jacoco test report"() {
        given:
        String reportXml = new JacocoXmlLoader().serverApp()

        when:
        Report report = new JacocoXmlReportParser().parseReport(reportXml)

        then:
        report.packages.size() == 18

        and:
        ReportPackage cleanupPackage = report.packages.find { it.name == "projektor/cleanup" }
        cleanupPackage != null

        and:
        cleanupPackage.sourceFiles.size() == 3

        SourceFile cleanupServiceFile = cleanupPackage.sourceFiles.find { it.name == "CleanupService.kt" }

        cleanupServiceFile.lines.size() == 27

        cleanupServiceFile.lines.find { it.number == 30 }.lineType() == LineType.PARTIAL
        cleanupServiceFile.lines.find { it.number == 58 }.lineType() == LineType.MISSED
        cleanupServiceFile.lines.find { it.number == 59 }.lineType() == LineType.MISSED
        cleanupServiceFile.lines.find { it.number == 60 }.lineType() == LineType.MISSED
        cleanupServiceFile.lines.find { it.number == 61 }.lineType() == LineType.COVERED

        Counter lineCounter = cleanupServiceFile.counters.find { it.type == CounterType.LINE }
        lineCounter.covered == 24
        lineCounter.missed == 3

        Counter branchCounter = cleanupServiceFile.counters.find { it.type == CounterType.BRANCH }
        branchCounter.covered == 9
        branchCounter.missed == 3
    }
}
