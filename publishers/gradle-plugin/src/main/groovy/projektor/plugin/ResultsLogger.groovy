package projektor.plugin

import org.gradle.api.logging.Logger

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
