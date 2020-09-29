package projektor.parser

import io.kotest.core.spec.style.StringSpec
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.contains
import strikt.assertions.hasSize

class ResultsXmlLoaderSpec : StringSpec({
    "should load passing spec" {
        val passingSpec = ResultsXmlLoader().passing()

        expectThat(passingSpec).contains("PassingSpec")
    }

    "should get Cypress results" {
        val cypressResults = ResultsXmlLoader().cypressResults()

        expectThat(cypressResults)
            .hasSize(6)
            .all {
                contains("<testsuite")
                not().contains("<testsuites")
            }
    }
})
