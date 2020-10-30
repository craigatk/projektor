package projektor.parser.jest

import projektor.parser.jest.model.Coverage
import projektor.parser.jest.model.CoverageFile
import projektor.parser.jest.model.CoverageLine
import projektor.parser.jest.model.CoveragePackage
import projektor.parser.jest.model.LineType
import projektor.server.example.coverage.JestXmlLoader
import spock.lang.Specification

class JestXmlReportParserFileSpec extends Specification {
    def "should parse file-level coverage details"() {
        given:
        String reportXml = new JestXmlLoader().ui()

        when:
        Coverage coverage = new JestXmlReportParser().parseReport(reportXml)

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
