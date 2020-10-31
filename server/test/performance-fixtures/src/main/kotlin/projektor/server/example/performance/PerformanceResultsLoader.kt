package projektor.server.example.performance

class PerformanceResultsLoader {
    fun k6tGetFailedTestCasesLarge() = loadTextFromFile("k6/getFailedTestCasesLarge.json")

    fun loadTextFromFile(filename: String) = javaClass
        .getResourceAsStream("/$filename")
        .bufferedReader()
        .readText()
}
