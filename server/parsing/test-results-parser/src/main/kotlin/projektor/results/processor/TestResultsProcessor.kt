package projektor.results.processor

import projektor.parser.JUnitResultsParser
import projektor.parser.model.TestSuite
import projektor.results.processor.ResultsXmlMerger.cleanAndMergeBlob

class TestResultsProcessor {
    private val junitResultsParser = JUnitResultsParser()

    /**
     * Handles parsing results XML docs in a variety of formats, including <testsuites>
     * results docs back-to-back with <xml> declarations in between.
     *
     * For example, handles:
     *
     * <?xml version="1.0" encoding="UTF-8"?>
     * <testsuites>
     *   <testsuite>
     *   </testsuite>
     * </testsuites>
     * <?xml version="1.0" encoding="UTF-8"?>
     * <testsuites>
     *   <testsuite>
     *   </testsuite>
     * </testsuites>
     *
     * or
     *
     * <?xml version="1.0" encoding="UTF-8"?>
     * <testsuites>
     *   <testsuite>
     *   </testsuite>
     * </testsuites>
     *
     * or
     *
     * <?xml version="1.0" encoding="UTF-8"?>
     * <testsuite>
     * </testsuite>
     *
     * @param resultsBlob
     * @return
     */
    fun parseResultsBlob(resultsBlob: String?): List<TestSuite> {
        return if (!resultsBlob.isNullOrEmpty()) {
            val resultsGroup = cleanAndMergeBlob(resultsBlob)

            junitResultsParser.parseTestSuites(resultsGroup)
        } else {
            listOf()
        }
    }
}
