package projektor.parser.coverage.go

import projektor.parser.coverage.go.model.GoCoverageFile
import projektor.parser.coverage.go.model.GoCoverageReport
import projektor.server.example.coverage.GoCoverageLoader
import projektor.server.example.coverage.JacocoXmlLoader
import spock.lang.Specification
import spock.lang.Unroll

class GoCoverageReportParserSpec extends Specification {
    def "should parse simple Go coverage file"() {
        given:
        String coverageReport = new GoCoverageLoader().simpleCoverage()

        when:
        GoCoverageReport report = GoCoverageReportParser.INSTANCE.parseReport(coverageReport)

        then:
        report != null
        report.mode == "set"
        report.files.size() == 1

        and:
        GoCoverageFile file = report.files[0]
        file.filePath == "github.com/user/project/pkg/calculator/calculator.go"
        file.fileName == "calculator.go"
        file.directoryName == "github.com/user/project/pkg/calculator"
        file.blocks.size() == 4

        and:
        file.coveredLines.containsAll([5, 6, 7, 9, 10, 11])
        file.missedLines.containsAll([13, 14, 15, 17, 18, 19])

        and:
        file.coveredStatements == 2
        file.missedStatements == 2
        file.totalStatements == 4
    }

    def "should parse multi-file Go coverage"() {
        given:
        String coverageReport = new GoCoverageLoader().multiFileCoverage()

        when:
        GoCoverageReport report = GoCoverageReportParser.INSTANCE.parseReport(coverageReport)

        then:
        report != null
        report.mode == "count"
        report.files.size() == 3

        and:
        GoCoverageFile calculatorFile = report.files.find { it.fileName == "calculator.go" }
        calculatorFile != null
        calculatorFile.blocks.size() == 4

        and:
        GoCoverageFile utilsFile = report.files.find { it.fileName == "utils.go" }
        utilsFile != null
        utilsFile.blocks.size() == 3

        and:
        GoCoverageFile mainFile = report.files.find { it.fileName == "main.go" }
        mainFile != null
        mainFile.blocks.size() == 2
    }

    def "should parse atomic mode coverage"() {
        given:
        String coverageReport = new GoCoverageLoader().atomicModeCoverage()

        when:
        GoCoverageReport report = GoCoverageReportParser.INSTANCE.parseReport(coverageReport)

        then:
        report != null
        report.mode == "atomic"
        report.files.size() == 1

        and:
        GoCoverageFile file = report.files[0]
        file.fileName == "handler.go"
        file.coveredStatements == 6
        file.missedStatements == 2
    }

    @Unroll
    def "#description should be Go coverage report #shouldBeGo"() {
        expect:
        GoCoverageReportParser.INSTANCE.isGoCoverageReport(reportContent) == shouldBeGo

        where:
        description           | reportContent                               || shouldBeGo
        "Simple Go coverage"  | new GoCoverageLoader().simpleCoverage()     || true
        "Multi-file coverage" | new GoCoverageLoader().multiFileCoverage()  || true
        "Atomic mode"         | new GoCoverageLoader().atomicModeCoverage() || true
        "Jacoco report"       | new JacocoXmlLoader().jacocoXmlParser()     || false
        "Empty string"        | ""                                          || false
        "Random text"         | "some random text"                          || false
    }

    def "should correctly identify covered vs missed lines"() {
        given:
        String coverageReport = """mode: set
github.com/test/file.go:1.1,3.2 1 1
github.com/test/file.go:5.1,5.2 1 0
github.com/test/file.go:7.1,10.2 2 1
"""

        when:
        GoCoverageReport report = GoCoverageReportParser.INSTANCE.parseReport(coverageReport)

        then:
        GoCoverageFile file = report.files[0]
        file.coveredLines.containsAll([1, 2, 3, 7, 8, 9, 10])
        file.missedLines == [5] as Set
    }
}
