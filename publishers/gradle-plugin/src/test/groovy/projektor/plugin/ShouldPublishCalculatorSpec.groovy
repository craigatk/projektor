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
        ShouldPublishCalculator.shouldPublishResults(extension, buildResult, resultsDataExists, coverageTasksExecuted, env) == shouldPublishResults

        where:
        scenario                                                                                          | extension                                                                                              | buildResult  | resultsDataExists | coverageTasksExecuted | env             || shouldPublishResults
        'passing build with publish on failure only set to false'                                         | new ProjektorPublishPluginExtension(autoPublishOnFailureOnly: false)                                   | passingBuild | true              | false                 | [:]             || true
        'failing build with publish on failure only set to false'                                         | new ProjektorPublishPluginExtension(autoPublishOnFailureOnly: false)                                   | failingBuild | true              | false                 | [:]             || true
        'build with no test or coverage results and publish on failure only set to false'                 | new ProjektorPublishPluginExtension(autoPublishOnFailureOnly: false)                                   | failingBuild | false             | false                 | [:]             || false
        'passing build with publish on failure only set to true'                                          | new ProjektorPublishPluginExtension(autoPublishOnFailureOnly: true)                                    | passingBuild | true              | false                 | [:]             || false
        'failing build with publish on failure only set to true'                                          | new ProjektorPublishPluginExtension(autoPublishOnFailureOnly: true)                                    | failingBuild | true              | false                 | [:]             || true
        'passing build with publish coverage in CI set to true and coverage executed'                     | new ProjektorPublishPluginExtension(autoPublishOnFailureOnly: true, autoPublishWhenCoverageInCI: true) | passingBuild | true              | true                  | ["CI": "true"]  || true
        'passing build with publish coverage in CI set to true and coverage executed but no test results' | new ProjektorPublishPluginExtension(autoPublishOnFailureOnly: true, autoPublishWhenCoverageInCI: true) | passingBuild | false             | true                  | ["CI": "true"]  || true
        'passing build with publish coverage in CI set to a text value and coverage executed'             | new ProjektorPublishPluginExtension(autoPublishOnFailureOnly: true, autoPublishWhenCoverageInCI: true) | passingBuild | true              | true                  | ["CI": "SYS"]   || true
        'passing build with publish coverage in CI set to false and coverage executed'                    | new ProjektorPublishPluginExtension(autoPublishOnFailureOnly: true, autoPublishWhenCoverageInCI: true) | passingBuild | true              | true                  | ["CI": "false"] || false
        'passing build with publish coverage in CI set to true and no coverage executed'                  | new ProjektorPublishPluginExtension(autoPublishOnFailureOnly: true, autoPublishWhenCoverageInCI: true) | passingBuild | true              | false                 | ["CI": "true"]  || false
        'passing build with publish coverage in CI set to true and coverage executed but not in CI'       | new ProjektorPublishPluginExtension(autoPublishOnFailureOnly: true, autoPublishWhenCoverageInCI: true) | passingBuild | true              | true                  | [:]             || false
    }
}
