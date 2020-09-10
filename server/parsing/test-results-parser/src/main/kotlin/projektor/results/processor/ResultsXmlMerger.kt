package projektor.results.processor

object ResultsXmlMerger {
    fun cleanAndMergeBlob(resultsBlob: String): String = resultsBlob
            .let(ResultsXmlMerger::removeXmlHeader)
            .let(ResultsXmlMerger::removeTestSuitesWrapper)
            .let(ResultsXmlMerger::wrappedInTestSuitesXml)

    fun removeTestSuitesWrapper(resultsXml: String): String {
        return resultsXml
                .replace(Regex("<testsuites.*?>"), "")
                .replace(Regex("</testsuites>"), "")
                .trim()
    }

    private fun removeXmlHeader(resultXml: String) = resultXml
            .replace(Regex("""<\?xml version="1.0" encoding="[Uu][Tt][Ff]-8"\?>"""), "")

    private fun wrappedInTestSuitesXml(resultsXml: String) = """<?xml version="1.0" encoding="UTF-8"?>
            <testsuites>
            $resultsXml
            </testsuites>
            """.trimIndent()
}
