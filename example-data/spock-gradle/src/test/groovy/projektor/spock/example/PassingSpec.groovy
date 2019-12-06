package projektor.example.spock

import spock.lang.Specification

class PassingSpec extends Specification {
    def "should pass"() {
        expect:
        1 == 1
    }
}