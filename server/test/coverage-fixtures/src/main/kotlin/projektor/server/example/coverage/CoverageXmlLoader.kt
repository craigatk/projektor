package projektor.server.example.coverage

abstract class CoverageXmlLoader {
    fun loadTextFromFile(filename: String) =
        javaClass
            .getResourceAsStream("/$filename")
            .bufferedReader()
            .readText()
}
