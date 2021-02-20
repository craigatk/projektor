package projektor.results.processor

import io.kotest.core.spec.style.StringSpec
import projektor.parser.ResultsXmlLoader
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.startsWith

class ResultsXmlMergerSpec : StringSpec({
    "when multiple test suites in blob with single-quoted XML declaration should replace them with single declaration" {
        val resultsBlob = ResultsXmlLoader().singleQuoteXmlDeclaration() + ResultsXmlLoader().singleQuoteXmlDeclaration()

        val mergedBlob = ResultsXmlMerger.cleanAndMergeBlob(resultsBlob)

        expectThat(mergedBlob).startsWith("""<?xml version="1.0" encoding="UTF-8"?>""")
        expectThat(mergedBlob.indexOf("<?xml")).isEqualTo(mergedBlob.lastIndexOf("<?xml"))
    }

    "when no encoding should replace it with single declaration" {
        val resultsBlob = ResultsXmlLoader().k6Example()

        val mergedBlob = ResultsXmlMerger.cleanAndMergeBlob(resultsBlob)

        expectThat(mergedBlob).startsWith("""<?xml version="1.0" encoding="UTF-8"?>""")
        expectThat(mergedBlob.indexOf("<?xml")).isEqualTo(mergedBlob.lastIndexOf("<?xml"))
    }

    "when regular encoding should replace it with single declaration" {
        val resultsBlob = ResultsXmlLoader().failing()

        val mergedBlob = ResultsXmlMerger.cleanAndMergeBlob(resultsBlob)

        expectThat(mergedBlob).startsWith("""<?xml version="1.0" encoding="UTF-8"?>""")
        expectThat(mergedBlob.indexOf("<?xml")).isEqualTo(mergedBlob.lastIndexOf("<?xml"))
    }
})
