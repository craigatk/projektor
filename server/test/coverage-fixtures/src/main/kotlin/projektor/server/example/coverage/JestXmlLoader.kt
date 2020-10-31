package projektor.server.example.coverage

class JestXmlLoader : CoverageXmlLoader() {
    fun uiClover() = loadTextFromFile("ui-clover.xml")

    fun uiClover2() = loadTextFromFile("ui-clover-2.xml")
}
