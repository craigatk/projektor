package projektor.server.example.coverage

class JestXmlLoader : CoverageXmlLoader() {
    fun ui() = loadTextFromFile("ui-clover.xml")

    fun ui2() = loadTextFromFile("ui-clover-2.xml")
}
