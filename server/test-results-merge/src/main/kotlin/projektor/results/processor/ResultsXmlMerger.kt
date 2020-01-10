package projektor.results.processor

object ResultsXmlMerger {
    @JvmStatic
    fun removeXmlHeader(resultXml: String) = resultXml.replace("""<?xml version="1.0" encoding="UTF-8"?>""", "")

    @JvmStatic
    fun wrappedInTestSuitesXml(resultsXmlList: List<String>): String =
            resultsXmlList
                    .map { removeXmlHeader(it) }
                    .joinToString("")
                    .let(ResultsXmlMerger::wrappedInTestSuitesXml)

    @JvmStatic
    fun wrappedInTestSuitesXml(resultsXml: String) = """<?xml version="1.0" encoding="UTF-8"?>
            <testsuites>
            $resultsXml
            </testsuites>
            """.trimIndent()

    @JvmStatic
    fun removeTestSuitesWrapper(resultsXml: String): String {
        return resultsXml
                .replace(Regex("<testsuites.*>"), "")
                .replace(Regex("</testsuites>"), "")
                .trim()
    }

    @JvmStatic
    fun cleanAndMergeBlob(resultsBlob: String): String = resultsBlob
            .let(ResultsXmlMerger::removeXmlHeader)
            .let(ResultsXmlMerger::removeTestSuitesWrapper)
            .let(ResultsXmlMerger::wrappedInTestSuitesXml)
}
