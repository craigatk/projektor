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
    def "should-publish-results should be #expectedPublish in scenario #scenario"() {
        expect:
        ShouldPublishCalculator.shouldPublishResults(extension, buildResult, coverageTasksExecuted, env) == shouldPublishResults

        where:
        scenario                                                                                    | extension                                                                                              | buildResult  | coverageTasksExecuted | env             || shouldPublishResults
        'passing build with publish on failure only set to false'                                   | new ProjektorPublishPluginExtension(autoPublishOnFailureOnly: false)                                   | passingBuild | false                 | [:]             || true
        'failing build with publish on failure only set to false'                                   | new ProjektorPublishPluginExtension(autoPublishOnFailureOnly: false)                                   | failingBuild | false                 | [:]             || true
        'passing build with publish on failure only set to true'                                    | new ProjektorPublishPluginExtension(autoPublishOnFailureOnly: true)                                    | passingBuild | false                 | [:]             || false
        'failing build with publish on failure only set to true'                                    | new ProjektorPublishPluginExtension(autoPublishOnFailureOnly: true)                                    | failingBuild | false                 | [:]             || true
        'passing build with publish coverage in CI set to true and coverage executed'               | new ProjektorPublishPluginExtension(autoPublishOnFailureOnly: true, autoPublishWhenCoverageInCI: true) | passingBuild | true                  | ["CI": "true"]  || true
        'passing build with publish coverage in CI set to a text value and coverage executed'       | new ProjektorPublishPluginExtension(autoPublishOnFailureOnly: true, autoPublishWhenCoverageInCI: true) | passingBuild | true                  | ["CI": "SYS"]   || true
        'passing build with publish coverage in CI set to false and coverage executed'              | new ProjektorPublishPluginExtension(autoPublishOnFailureOnly: true, autoPublishWhenCoverageInCI: true) | passingBuild | true                  | ["CI": "false"] || false
        'passing build with publish coverage in CI set to true and no coverage executed'            | new ProjektorPublishPluginExtension(autoPublishOnFailureOnly: true, autoPublishWhenCoverageInCI: true) | passingBuild | false                 | ["CI": "true"]  || false
        'passing build with publish coverage in CI set to true and coverage executed but not in CI' | new ProjektorPublishPluginExtension(autoPublishOnFailureOnly: true, autoPublishWhenCoverageInCI: true) | passingBuild | true                  | [:]             || false
    }
}
