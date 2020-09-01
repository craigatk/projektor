package projektor.server.example.coverage

class JacocoXmlLoader {
    fun jacocoXmlParser() = loadTextFromFile("jacoco-xml-parser-jacocoTestReport.xml")
    fun jacocoXmlParserReduced() = loadTextFromFile("jacoco-xml-parser-reduced-jacocoTestReport.xml")

    fun junitResultsParser() = loadTextFromFile("junit-results-parser-jacocoTestReport.xml")
    fun junitResultsParserReduced() = loadTextFromFile("junit-results-parser-reduced-jacocoTestReport.xml")

    fun serverApp() = loadTextFromFile("server-app-jacocoTestReport.xml")
    fun serverAppReduced() = loadTextFromFile("server-app-reduced-jacocoTestReport.xml")

    private fun loadTextFromFile(filename: String) = javaClass
            .getResourceAsStream("/$filename")
            .bufferedReader()
            .readText()
}
