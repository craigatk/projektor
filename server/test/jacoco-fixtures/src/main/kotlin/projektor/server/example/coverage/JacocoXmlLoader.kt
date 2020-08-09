package projektor.server.example.coverage

class JacocoXmlLoader {
    fun serverApp() = loadTextFromFile("server-app-jacocoTestReport.xml")

    private fun loadTextFromFile(filename: String) = javaClass
            .getResourceAsStream("/$filename")
            .bufferedReader()
            .readText()
}
