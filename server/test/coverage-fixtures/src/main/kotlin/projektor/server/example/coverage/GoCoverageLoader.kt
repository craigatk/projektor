package projektor.server.example.coverage

class GoCoverageLoader : CoverageXmlLoader() {
    fun simpleCoverage() = loadTextFromFile("go/simple-coverage.out")

    fun multiFileCoverage() = loadTextFromFile("go/multi-file-coverage.out")

    fun atomicModeCoverage() = loadTextFromFile("go/atomic-mode-coverage.out")
}
