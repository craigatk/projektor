package projektor.server.example.coverage

class JacocoXmlLoader {
    fun jacocoXmlParser() = loadTextFromFile("jacoco-xml-parser-jacocoTestReport.xml")

    fun junitResultsParser() = loadTextFromFile("junit-results-parser-jacocoTestReport.xml")

    fun serverApp() = loadTextFromFile("server-app-jacocoTestReport.xml")

    private fun loadTextFromFile(filename: String) = javaClass
            .getResourceAsStream("/$filename")
            .bufferedReader()
            .readText()
}
