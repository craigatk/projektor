package projektor.plugin.coverage

import org.gradle.api.Task
import org.gradle.api.logging.Logger
import spock.lang.Specification

class CodeCoverageTaskCollectorSpec extends Specification {
    Logger logger = Mock(Logger)
    CodeCoverageTaskCollector taskCollector = new CodeCoverageTaskCollector([], true, logger)

    void 'koverCoverageFileOrNull can handle a null xmlReportFile'() {
        given:
        Task task = Mock(Task)

        when:
        taskCollector.koverCoverageFileOrNull(null)

        then:
        1 * logger.info("Unable to set Projektor Kover coverage: Found no source files or source directories.")
        0 * _
    }
}
