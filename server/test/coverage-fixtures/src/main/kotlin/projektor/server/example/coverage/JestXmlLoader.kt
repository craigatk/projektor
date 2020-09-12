package projektor.server.example.coverage

class JestXmlLoader : CoverageXmlLoader() {
    fun ui() = loadTextFromFile("ui-clover.xml")
}
