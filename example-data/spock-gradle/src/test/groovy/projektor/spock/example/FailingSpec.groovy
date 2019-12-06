package projektor.example.spock

import spock.lang.Specification

class FailingSpec extends Specification {
    def "should fail"() {
        expect:
        1 == 2
    }

    def "should fail with output"() {
        given:
        println "A line in the given block"
        println "Another line in the given block"

        when:
        println "A line in the when block"
        int actual = 1

        then:
        println "A line in the then block"
        actual == 3
    }
}