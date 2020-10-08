package projektor.results.processor

import io.kotest.core.spec.style.StringSpec
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ResultsXmlMergerSpec : StringSpec({
    "should remove testsuites wrapper element from result" {
        val resultsXml = """<testsuites name="Mocha Tests" time="6.475" tests="2" failures="0">
<testsuite name="Root Suite" timestamp="2019-10-03T11:49:11" tests="0" failures="0" time="0">
</testsuite>
</testsuites>"""

        val resultsWithTestSuitesRemoved = ResultsXmlMerger.removeTestSuitesWrapper(resultsXml)

        expectThat(resultsWithTestSuitesRemoved).isEqualTo(
            """<testsuite name="Root Suite" timestamp="2019-10-03T11:49:11" tests="0" failures="0" time="0">
</testsuite>"""
        )
    }

    "should remove testsuites wrapper element and whitespace from result" {
        val resultsXml = """<testsuites name="Mocha Tests" time="6.475" tests="2" failures="0">
  <testsuite name="Root Suite" timestamp="2019-10-03T11:49:11" tests="0" failures="0" time="0">
</testsuite>

</testsuites>"""

        val resultsWithTestSuitesRemoved = ResultsXmlMerger.removeTestSuitesWrapper(resultsXml)

        expectThat(resultsWithTestSuitesRemoved).isEqualTo(
            """<testsuite name="Root Suite" timestamp="2019-10-03T11:49:11" tests="0" failures="0" time="0">
</testsuite>"""
        )
    }
})
