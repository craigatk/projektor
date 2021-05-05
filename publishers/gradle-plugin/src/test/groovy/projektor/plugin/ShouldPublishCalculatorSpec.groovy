package projektor.plugin

import org.gradle.BuildResult
import org.gradle.api.invocation.Gradle
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class ShouldPublishCalculatorSpec extends Specification {
    @Shared
    Gradle gradle = Mock()

    @Shared
    BuildResult passingBuild = new BuildResult(gradle, null)

    @Shared
    BuildResult failingBuild = new BuildResult(gradle, new IllegalStateException())

    @Unroll
    def "should-publish-results should be #shouldPublishResults in scenario #scenario"() {
        expect:
        ShouldPublishCalculator.shouldPublishResults(
                extension,
                passingBuild,
                resultsDataExists,
                false,
                environment
        ) == shouldPublishResults

        where:
        scenario                                                  | extension                                                | resultsDataExists | environment    || shouldPublishResults
        "has results and always publish=true"                     | new ProjektorPublishPluginExtension(alwaysPublish: true) | true              | [:]            || true
        "has no results and always publish=true"                  | new ProjektorPublishPluginExtension(alwaysPublish: true) | false             | [:]            || false
        "has results and always publish in CI=true and in CI"     | new ProjektorPublishPluginExtension()                    | true              | ["CI": "true"] || true
        "has no results and always publish in CI=true and in CI"  | new ProjektorPublishPluginExtension()                    | false             | ["CI": "true"] || false
        "has results and always publish in CI=true and not in CI" | new ProjektorPublishPluginExtension()                    | true              | [:]            || false
    }

    @Unroll
    def "should publish on local failure #shouldPublishResults in scenario #scenario"() {
        expect:
        ShouldPublishCalculator.shouldPublishResults(
                extension,
                buildResult,
                resultsDataExists,
                false,
                [:]
        ) == shouldPublishResults

        where:
        scenario                                            | extension                                                         | resultsDataExists | buildResult  || shouldPublishResults
        "failing local build"                               | new ProjektorPublishPluginExtension()                             | true              | failingBuild || true
        "failing local build with publish on local = false" | new ProjektorPublishPluginExtension(publishOnLocalFailure: false) | true              | failingBuild || false
        "passing local build"                               | new ProjektorPublishPluginExtension()                             | true              | passingBuild || false
    }

    @Unroll
    def "should publish based on code coverage #shouldPublishResults in scenario #scenario"() {
        expect:
        ShouldPublishCalculator.shouldPublishResults(
                extension,
                passingBuild,
                false,
                coverageTasksExecuted,
                environment
        ) == shouldPublishResults

        where:
        scenario                       | extension                             | coverageTasksExecuted | environment     || shouldPublishResults
        "in CI with coverage data"     | new ProjektorPublishPluginExtension() | true                  | ["CI": "true"]  || true
        "not in CI with coverage data" | new ProjektorPublishPluginExtension() | true                  | ["CI": "false"] || false
        "in CI without coverage data"  | new ProjektorPublishPluginExtension() | false                 | ["CI": "true"]  || false
    }

    def "should not publish when local with passing build and coverage but no results"() {
        expect:
        !ShouldPublishCalculator.shouldPublishResults(
                new ProjektorPublishPluginExtension(),
                passingBuild,
                false,
                true,
                [:]
        )
    }
}
