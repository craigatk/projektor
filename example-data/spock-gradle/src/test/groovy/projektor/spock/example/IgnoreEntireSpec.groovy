package projektor.example.spock

import spock.lang.Specification
import spock.lang.Ignore

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