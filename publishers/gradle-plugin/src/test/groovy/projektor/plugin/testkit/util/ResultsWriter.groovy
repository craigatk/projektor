package projektor.plugin.testkit.util

class ResultsWriter {
    File writeResults(File resultsDir, String fileName, String resultsXml) {
        File resultsFile = new File(resultsDir, fileName)

        resultsFile.text = resultsXml

        return resultsFile
    }
}
