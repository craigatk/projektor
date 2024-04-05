package projektor.server.example.coverage

class CloverXmlLoader : CoverageXmlLoader() {
    fun uiClover() = loadTextFromFile("ui-clover.xml")
    fun uiClover2() = loadTextFromFile("ui-clover-2.xml")
    fun uiCloverLarge() = loadTextFromFile("ui-clover-large.xml")
    fun uiCloverManyMissedLines() = loadTextFromFile("ui-clover-many-missed-lines.xml")
    fun noPackage() = loadTextFromFile("clover-no-package.xml")

    fun coverageFilesTable() = loadTextFromFile("clover-append/CoverageFilesTable-clover.xml")
    fun coverageGraph() = loadTextFromFile("clover-append/CoverageGraph-clover.xml")
    fun coverageTable() = loadTextFromFile("clover-append/CoverageTable-clover.xml")
}
