package projektor.results.processor

import projektor.parser.model.TestSuites

class CypressFileNamePostProcessor : TestSuitesPostProcessor {
    override fun postProcess(testSuites: TestSuites): TestSuites {
        val testSuiteList = testSuites.testSuites

        return if (testSuiteList != null) {
            val testSuiteWithFileName = testSuiteList.find { it.file != null }

            if (testSuiteWithFileName != null && testSuiteList.size > 1) {
                testSuiteList.forEach { testSuite -> testSuite.name = testSuiteWithFileName.file }
            }

            testSuites
        } else {
            testSuites
        }
    }
}
