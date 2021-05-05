package projektor.plugin

import spock.lang.Specification
import spock.lang.Unroll

class MetadataResolverSpec extends Specification {
    @Unroll
    def "should get build number #expectedBuildNumber with environment #environment"() {
        expect:
        MetadataResolver.findBuildNumber(environment, new ProjektorPublishPluginExtension()) == expectedBuildNumber

        where:
        environment                 || expectedBuildNumber
        ["VELA_BUILD_NUMBER": "42"] || "42"
        ["GITHUB_RUN_NUMBER": "24"] || "24"
        [:]                         || null
    }

    @Unroll
    def "should be CI #shouldBeCI with environment #environment"() {
        expect:
        MetadataResolver.isCI(environment, new ProjektorPublishPluginExtension()) == shouldBeCI

        where:
        environment                     || shouldBeCI
        ["CI": "true"]                  || true
        ["CI": "VELA"]                  || true
        ["CI": "false"]                 || false
        [:]                             || false
        ["DRONE": "true"]               || true
        ["VELA": "true"]                || true
        ["CI": "false", "VELA": "true"] || true
    }
}
