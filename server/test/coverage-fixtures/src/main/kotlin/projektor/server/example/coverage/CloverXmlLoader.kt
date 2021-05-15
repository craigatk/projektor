package projektor.server.example.coverage

class CloverXmlLoader : CoverageXmlLoader() {
    fun uiClover() = loadTextFromFile("ui-clover.xml")
    fun uiClover2() = loadTextFromFile("ui-clover-2.xml")

    fun coverageFilesTable() = loadTextFromFile("clover-append/CoverageFilesTable-clover.xml")
    fun coverageGraph() = loadTextFromFile("clover-append/CoverageGraph-clover.xml")
    fun coverageTable() = loadTextFromFile("clover-append/CoverageTable-clover.xml")
}
