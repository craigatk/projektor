package projektor.server.example.coverage

import java.math.BigDecimal

class JacocoXmlLoader : CoverageXmlLoader() {
    fun jacocoXmlParser() = loadTextFromFile("jacoco-xml-parser-jacocoTestReport.xml")
    fun jacocoXmlParserReduced() = loadTextFromFile("jacoco-xml-parser-reduced-jacocoTestReport.xml")
    fun jacocoXmlParser75() = loadTextFromFile("jacoco-xml-parser-75-jacocoTestReport.xml")
    fun jacocoXmlParser85() = loadTextFromFile("jacoco-xml-parser-85-jacocoTestReport.xml")

    fun junitResultsParser() = loadTextFromFile("junit-results-parser-jacocoTestReport.xml")
    fun junitResultsParserReduced() = loadTextFromFile("junit-results-parser-reduced-jacocoTestReport.xml")

    fun serverApp() = loadTextFromFile("server-app-jacocoTestReport.xml")
    fun serverAppReduced() = loadTextFromFile("server-app-reduced-jacocoTestReport.xml")
    fun serverAppInvalid() = loadTextFromFile("server-app-jacocoTestReport-invalid.xml")

    fun combinedIntegrationTest() = loadTextFromFile("jacoco-combined/integrationTestJacocoReport.xml")
    fun combinedUnitTest() = loadTextFromFile("jacoco-combined/jacocoTestReport.xml")

    companion object {
        val jacocoXmlParserLineCoveragePercentage = BigDecimal("92.86")

        val junitResultsParserLineCoveragePercentage = BigDecimal("92.31")

        val serverAppLineCoveragePercentage = BigDecimal("97.44")
        val serverAppReducedLineCoveragePercentage = BigDecimal("95.40")
    }
}
