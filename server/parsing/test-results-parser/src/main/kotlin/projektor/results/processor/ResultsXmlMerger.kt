package projektor.results.processor

object ResultsXmlMerger {
    fun cleanAndMergeBlob(resultsBlob: String): String = resultsBlob
        .let(ResultsXmlMerger::removeXmlHeader)
        .let(ResultsXmlMerger::conditionallyWrapInTestSuitesXml)
        .let(ResultsXmlMerger::wrappedInTestSuitesWrapperXml)

    fun conditionallyWrapInTestSuitesXml(resultsXml: String): String =
        if (!resultsXml.contains("<testsuites")) {
            """
               <testsuites>
               $resultsXml
                </testsuites>
            """.trimIndent()
        } else {
            resultsXml
        }

    private fun removeXmlHeader(resultXml: String) = resultXml
        .replace(Regex("""<\?xml.*\?>"""), "")

    private fun wrappedInTestSuitesWrapperXml(resultsXml: String) = """<?xml version="1.0" encoding="UTF-8"?>
            <testsuiteswrapper>
            $resultsXml
            </testsuiteswrapper>
    """.trimIndent()
}
