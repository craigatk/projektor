package projektor.plugin.results

import org.gradle.api.logging.Logger
import projektor.plugin.PublishResult

class ResultsLogger {
    private final Logger logger

    ResultsLogger(Logger logger) {
        this.logger = logger
    }

    void logReportResults(PublishResult publishResult) {
        if (publishResult.successful) {
            logger.error("\nView Projektor report at: ${publishResult.reportUrl}")
        }
    }
}
