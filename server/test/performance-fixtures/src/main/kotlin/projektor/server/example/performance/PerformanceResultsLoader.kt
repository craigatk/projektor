package projektor.server.example.performance

class PerformanceResultsLoader {
    fun k6GetFailedTestCasesLarge() = loadTextFromFile("k6/getFailedTestCasesLarge.json")

    fun k6GetRun() = loadTextFromFile("k6/getRun.json")

    fun loadTextFromFile(filename: String) =
        javaClass
            .getResourceAsStream("/$filename")
            .bufferedReader()
            .readText()
}
