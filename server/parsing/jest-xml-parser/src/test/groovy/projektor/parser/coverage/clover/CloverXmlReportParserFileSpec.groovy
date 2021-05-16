package projektor.parser.coverage.clover

import projektor.parser.coverage.clover.model.Coverage
import projektor.parser.coverage.clover.model.CoverageFile
import projektor.parser.coverage.clover.model.CoverageLine
import projektor.parser.coverage.clover.model.CoveragePackage
import projektor.parser.coverage.clover.model.LineType
import projektor.server.example.coverage.CloverXmlLoader
import spock.lang.Specification

class CloverXmlReportParserFileSpec extends Specification {
    def "should parse file-level coverage details"() {
        given:
        String reportXml = new CloverXmlLoader().uiClover()

        when:
        Coverage coverage = new CloverXmlReportParser().parseReport(reportXml)

        then:
        coverage.project.packages.size() == 22

        CoveragePackage codeTextPackage = coverage.project.packages.find { it.name == "src.CodeText" }
        codeTextPackage.files.size() == 3

        CoverageFile codeTextLineFile = codeTextPackage.files.find { it.name == "CodeTextLine.tsx" }
        codeTextLineFile.metrics.statements == 14
        codeTextLineFile.metrics.coveredStatements == 14
        codeTextLineFile.metrics.conditionals == 6
        codeTextLineFile.metrics.coveredConditionals == 5

        codeTextLineFile.lines.size() == 14

        // <line num="15" count="10" type="cond" truecount="2" falsecount="0"/>
        CoverageLine line15 = codeTextLineFile.lines.find { it.number == 15 }
        line15.lineType == "cond"
        line15.count == 10
        line15.trueCount == 2
        line15.falseCount == 0
        line15.lineCoverageType() == LineType.PARTIAL

        // <line num="38" count="6" type="stmt"/>
        CoverageLine line38 = codeTextLineFile.lines.find { it.number == 38 }
        line38.lineType == "stmt"
        line38.count == 6
        line38.lineCoverageType() == LineType.COVERED

        CoverageFile codeTextProgressiveRender = codeTextPackage.files.find { it.name == "CodeTextProgressiveRender.tsx" }
        codeTextProgressiveRender.lines.find { it.number == 59 }.lineCoverageType() == LineType.MISSED
    }
}
