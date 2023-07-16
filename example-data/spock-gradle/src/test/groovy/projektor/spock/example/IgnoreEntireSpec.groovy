package projektor.example.spock

import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class IgnoreEntireSpec extends Specification {
    def "should not run test case 1"() {
        expect:
        false
    }

    def "should not run test case 2"() {
        expect:
        false
    }

    def "should not run test case 3"() {
        expect:
        false
    }

    def "should not run test case 4"() {
        expect:
        false
    }
}