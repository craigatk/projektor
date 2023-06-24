package projektor.parser.coverage.cobertura

import projektor.parser.coverage.cobertura.model.Coverage
import projektor.parser.coverage.cobertura.model.CoverageClass
import projektor.parser.coverage.cobertura.model.CoverageLine
import projektor.parser.coverage.cobertura.model.Pkg
import projektor.server.example.coverage.CoberturaXmlLoader
import projektor.server.example.coverage.JacocoXmlLoader
import spock.lang.Specification
import spock.lang.Unroll

class CoberturaXmlReportParserSpec extends Specification {
    def "should parse coverage file"() {
        given:
        String reportXml = new CoberturaXmlLoader().uiCobertura()

        when:
        Coverage coverage = new CoberturaXmlReportParser().parseReport(reportXml)

        then:
        coverage != null

        coverage.linesValid == 1633
        coverage.linesCovered == 1491
        coverage.lineRate == 0.9129999999999999

        coverage.branchesValid == 367
        coverage.branchesCovered == 299
        coverage.branchRate == 0.8147

        and:
        List<Pkg> packages = coverage.packages.packages
        packages.size() == 33

        and:
        Pkg coveragePackage = packages.find { it.name == "src.Coverage" }
        coveragePackage != null

        List<CoverageClass> coverageClasses = coveragePackage.classes.clazz
        coverageClasses.size() == 11

        CoverageClass coverageDetailsClass = coverageClasses.find { it.fileName == "src/Coverage/CoverageDetails.tsx" }
        coverageDetailsClass != null
        coverageDetailsClass.name == "CoverageDetails.tsx"

        List<CoverageLine> coverageDetailsClassLines = coverageDetailsClass.lines.lines
        coverageDetailsClassLines.size() == 11

        coverageDetailsClassLines.find { it.number == 1 }?.covered
        coverageDetailsClassLines.find { it.number == 3 }?.covered
        coverageDetailsClassLines.find { it.number == 4 }?.covered
        coverageDetailsClassLines.find { it.number == 5 }?.covered
        coverageDetailsClassLines.find { it.number == 12 }?.covered
        coverageDetailsClassLines.find { it.number == 18 }?.covered
        !coverageDetailsClassLines.find { it.number == 19 }?.covered
        !coverageDetailsClassLines.find { it.number == 21 }?.covered
        !coverageDetailsClassLines.find { it.number == 23 }?.covered
        !coverageDetailsClassLines.find { it.number == 31 }?.covered
        coverageDetailsClassLines.find { it.number == 51 }?.covered
    }

    @Unroll
    def "#description should be Cobertura #shouldBeCobertura"() {
        expect:
        CoberturaXmlReportParser.isCoberturaReport(reportXml) == shouldBeCobertura

        where:
        description             | reportXml                                      || shouldBeCobertura
        "Node script Cobertura" | new CoberturaXmlLoader().nodeScriptCobertura() || true
        "UI Cobertura"          | new CoberturaXmlLoader().uiCobertura()         || true
        "Jacoco"                | new JacocoXmlLoader().jacocoXmlParser()        || false
    }

    def "should parse Cobertura report without branch field on line elements"() {
        given:
        String reportXml = new CoberturaXmlLoader().noBranchCobertura()

        when:
        Coverage coverage = new CoberturaXmlReportParser().parseReport(reportXml)

        then:
        coverage != null

        List<Pkg> packages = coverage.packages.packages
        List<CoverageClass> classes = packages.classes.clazz
        List<CoverageLine> lines = classes.collect { it.lines.lines }.flatten()

        lines.size() == 4
        lines.every { !it.isPartial() }
    }
}
