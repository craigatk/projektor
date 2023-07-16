package projektor.example.spock

import spock.lang.Ignore
import spock.lang.Specification

class IgnoreSomeMethodsSpec extends Specification {
    def "should run test case 1"() {
        expect:
        true
    }

    def "should run test case 2"() {
        expect:
        true
    }

    @Ignore
    def "should not run test case 3"() {
        expect:
        true
    }

    def "should run test case 4"() {
        expect:
        true
    }

    def "should run test case 5"() {
        expect:
        true
    }

    @Ignore
    def "should not run test case 6"() {
        expect:
        true
    }

    @Ignore
    def "should not run test case 7"() {
        expect:
        true
    }

    def "should run test case 8"() {
        expect:
        true
    }

    def "should run test case 9"() {
        expect:
        true
    }

    def "should run test case 10"() {
        expect:
        true
    }
}