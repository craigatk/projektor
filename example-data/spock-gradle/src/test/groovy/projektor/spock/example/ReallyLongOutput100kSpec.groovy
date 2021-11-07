package projektor.example.spock

import spock.lang.Specification

class ReallyLongOutput100kSpec extends Specification {
    def "should include lots of system out and system err"() {
        given:
        String longString = (1..100).collect { "$it" }.join("")
        println longString
        100000.times { println "System out line $it"}

        when:
        int actual = 1

        then:
        100000.times { System.err.println("System err line $it") }
        actual == 1
    }
}
