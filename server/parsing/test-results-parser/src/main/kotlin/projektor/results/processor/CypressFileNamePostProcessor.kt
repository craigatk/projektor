package projektor.results.processor

import projektor.parser.model.TestSuites

class CypressFileNamePostProcessor : TestSuitesPostProcessor {
    override fun postProcess(testSuites: TestSuites): TestSuites {
        val testSuiteList = testSuites.testSuites

        return if (testSuiteList != null) {
            val testSuiteWithFileName = testSuiteList.find { it.file != null }

            if (testSuiteWithFileName != null && testSuiteList.size > 1) {
                // Apply the file name from the first test suite
                testSuiteList.forEach { testSuite -> testSuite.file = testSuiteWithFileName.file }

                if (testSuiteWithFileName.name.toLowerCase() != "root suite") {
                    testSuiteList.forEach { testSuite -> testSuite.name = testSuiteWithFileName.name }
                }
            }

            testSuites
        } else {
            testSuites
        }
    }
}
