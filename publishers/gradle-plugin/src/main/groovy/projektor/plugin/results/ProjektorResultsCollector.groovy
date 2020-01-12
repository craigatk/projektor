package projektor.plugin.results

import org.gradle.api.logging.Logger

class ProjektorResultsCollector {
    private final Logger logger

    ProjektorResultsCollector(Logger logger) {
        this.logger = logger
    }

    String createResultsBlobFromJunitXmlResultsInDirectories(List<File> junitXmlResultsDirectories) {
        List<File> allJunitXmlFiles = junitXmlResultsDirectories.collect {
            findJunitXmlReportsInDirectory(it)
        }.flatten()

        logger.info("Found ${allJunitXmlFiles.size()} JUnit XML results files in ${junitXmlResultsDirectories.size()} directories")

        String resultsBlob = createResultsBlob(allJunitXmlFiles)

        return resultsBlob
    }

    private static List<File> findJunitXmlReportsInDirectory(File reportDirectory) {
        return reportDirectory.listFiles(new FilenameFilter() {
            @Override
            boolean accept(File dir, String name) {
                name.endsWith(".xml")
            }
        })?.toList() ?: []
    }

    private static String createResultsBlob(List<File> resultsFiles) {
        def texts = resultsFiles.collect { it.text }
        return texts.join("\n")
    }
}
