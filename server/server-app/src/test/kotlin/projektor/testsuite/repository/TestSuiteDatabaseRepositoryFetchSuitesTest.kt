package projektor.testsuite.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.testsuite.TestSuiteDatabaseRepository
import projektor.testsuite.TestSuiteSearchCriteria
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

class TestSuiteDatabaseRepositoryFetchSuitesTest : DatabaseRepositoryTestCase() {
    @Test
    fun `should fetch test suites with specific package name`() {
        val testSuiteDatabaseRepository = TestSuiteDatabaseRepository(dslContext)
        val publicId = randomPublicId()

        testRunDBGenerator.createTestRun(
            publicId,
            listOf(
                TestSuiteData(
                    "projektor.TestSuite1",
                    listOf("testCase1"),
                    listOf(),
                    listOf()
                ),
                TestSuiteData(
                    "projektor.TestSuite2",
                    listOf("testCase1"),
                    listOf(),
                    listOf()
                ),
                TestSuiteData(
                    "something.else.testSuite2",
                    listOf("testCase1"),
                    listOf(),
                    listOf()
                )
            )
        )

        val testSuites = runBlocking {
            testSuiteDatabaseRepository.fetchTestSuites(publicId, TestSuiteSearchCriteria("projektor", false))
        }

        expectThat(testSuites)
            .hasSize(2)
            .any {
                get { packageName }.isNotNull().isEqualTo("projektor")
                get { className }.isEqualTo("TestSuite1")
            }
            .any {
                get { packageName }.isNotNull().isEqualTo("projektor")
                get { className }.isEqualTo("TestSuite2")
            }
    }

    @Test
    fun `when no test suites match package name should return empty list`() {
        val testSuiteDatabaseRepository = TestSuiteDatabaseRepository(dslContext)
        val publicId = randomPublicId()

        testRunDBGenerator.createTestRun(
            publicId,
            listOf(
                TestSuiteData(
                    "projektor.TestSuite1",
                    listOf("testCase1"),
                    listOf(),
                    listOf()
                ),
                TestSuiteData(
                    "projektor.TestSuite2",
                    listOf("testCase1"),
                    listOf(),
                    listOf()
                ),
                TestSuiteData(
                    "something.else.testSuite2",
                    listOf("testCase1"),
                    listOf(),
                    listOf()
                )
            )
        )

        val testSuites = runBlocking {
            testSuiteDatabaseRepository.fetchTestSuites(publicId, TestSuiteSearchCriteria("should.find.nothing", false))
        }

        expectThat(testSuites).isEmpty()
    }
}
