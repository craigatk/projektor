package projektor.results.processor

import projektor.parser.model.TestSuites

interface TestSuitesPostProcessor {
    fun postProcess(testSuites: TestSuites): TestSuites
}
