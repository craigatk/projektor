package projecktor.results.merge

object ResultsXmlMerger {
    @JvmStatic
    fun removeXmlHeader(resultXml: String) = resultXml.replace("""<?xml version="1.0" encoding="UTF-8"?>""", "")

    @JvmStatic
    fun wrappedInTestSuitesXml(resultsXmlList: List<String>): String =
            resultsXmlList
                    .map { removeXmlHeader(it) }
                    .joinToString("")
                    .let(::wrappedInTestSuitesXml)

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
            .let(::removeXmlHeader)
            .let(::removeTestSuitesWrapper)
            .let(::wrappedInTestSuitesXml)
}
