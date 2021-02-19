package projektor.plugin.coverage

import org.gradle.BuildResult
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.logging.Logger
import org.gradle.execution.taskgraph.DefaultTaskExecutionGraph
import spock.lang.Specification
import org.gradle.api.invocation.Gradle

class CodeCoverageTaskCollectorSpec extends Specification {
    def "should not fail if task graph not ready"() {
        given:
        TaskExecutionGraph taskExecutionGraph = new DefaultTaskExecutionGraph(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        )
        Gradle gradle = Mock()

        BuildResult buildResult = new BuildResult("test", gradle, null)
        boolean coverageEnabled = true
        Logger logger = Mock()

        when:
        CodeCoverageTaskCollector codeCoverageTaskCollector = new CodeCoverageTaskCollector(buildResult, coverageEnabled, logger)

        then:
        1 * gradle.taskGraph >> taskExecutionGraph

        and:
        !codeCoverageTaskCollector.hasCodeCoverageData()
        codeCoverageTaskCollector.codeCoverageFiles.empty
    }
}
