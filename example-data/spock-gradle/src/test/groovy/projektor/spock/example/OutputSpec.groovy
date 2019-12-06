package projektor.example.spock

import spock.lang.Specification

class OutputSpec extends Specification {
    def "should include system out and system err"() {
        given:
        100.times { println "System out line $it"}

        when:
        int actual = 1

        then:
        200.times { System.err.println("System err line $it") }
        actual == 1
    }
}