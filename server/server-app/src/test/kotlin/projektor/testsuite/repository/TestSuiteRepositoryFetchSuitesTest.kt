package projektor.testsuite.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.persistence.DatabaseTestBackend
import projektor.persistence.ObjectStoreTestBackend
import projektor.persistence.RepositoryTestCase
import projektor.testsuite.TestSuiteSearchCriteria
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

abstract class TestSuiteRepositoryFetchSuitesTest : RepositoryTestCase() {
    @Test
    fun `should fetch test suites with specific package name`() {
        val publicId = randomPublicId()

        testRunGenerator.createTestRun(
            publicId,
            listOf(
                TestSuiteData(
                    "projektor.TestSuite1",
                    listOf("testCase1"),
                    listOf(),
                    listOf(),
                ),
                TestSuiteData(
                    "projektor.TestSuite2",
                    listOf("testCase1"),
                    listOf(),
                    listOf(),
                ),
                TestSuiteData(
                    "something.else.testSuite2",
                    listOf("testCase1"),
                    listOf(),
                    listOf(),
                ),
            ),
        )

        val testSuites =
            runBlocking {
                testSuiteRepository.fetchTestSuites(publicId, TestSuiteSearchCriteria("projektor", false))
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
        val publicId = randomPublicId()

        testRunGenerator.createTestRun(
            publicId,
            listOf(
                TestSuiteData(
                    "projektor.TestSuite1",
                    listOf("testCase1"),
                    listOf(),
                    listOf(),
                ),
                TestSuiteData(
                    "projektor.TestSuite2",
                    listOf("testCase1"),
                    listOf(),
                    listOf(),
                ),
                TestSuiteData(
                    "something.else.testSuite2",
                    listOf("testCase1"),
                    listOf(),
                    listOf(),
                ),
            ),
        )

        val testSuites =
            runBlocking {
                testSuiteRepository.fetchTestSuites(publicId, TestSuiteSearchCriteria("should.find.nothing", false))
            }

        expectThat(testSuites).isEmpty()
    }
}

class TestSuiteRepositoryFetchSuitesDatabaseTest : TestSuiteRepositoryFetchSuitesTest() {
    override fun createBackend() = DatabaseTestBackend()
}

class TestSuiteRepositoryFetchSuitesObjectStoreTest : TestSuiteRepositoryFetchSuitesTest() {
    override fun createBackend() = ObjectStoreTestBackend()
}
