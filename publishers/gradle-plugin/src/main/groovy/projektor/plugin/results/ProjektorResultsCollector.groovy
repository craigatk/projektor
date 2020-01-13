package projektor.plugin.results

import org.gradle.api.logging.Logger

class ProjektorResultsCollector {
    private final Logger logger

    ProjektorResultsCollector(Logger logger) {
        this.logger = logger
    }

    String createResultsBlobFromJunitXmlResultsInDirectory(File junitXmlResultsDirectory) {
        List<File> junitXmlFiles =  findJunitXmlReportsInDirectory(junitXmlResultsDirectory)

        logger.info("Found ${junitXmlFiles.size()} JUnit XML results files in ${junitXmlResultsDirectory.name} directory")

        String resultsBlob = createResultsBlob(junitXmlFiles)

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
