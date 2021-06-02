package projektor.testsuite.repository

import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.testsuite.TestSuiteDatabaseRepository
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

class TestSuiteDatabaseRepositoryAppendTest : DatabaseRepositoryTestCase() {

    @Test
    fun `should fetch highest test suite index`() {
        val testSuiteDatabaseRepository = TestSuiteDatabaseRepository(dslContext)
        val publicId = randomPublicId()

        testRunDBGenerator.createTestRun(
            publicId,
            listOf(
                TestSuiteData(
                    "testSuite1",
                    listOf("testSuite1PassedTestCase1"),
                    listOf("testSuite1FailedTestCase1", "testSuite1FailedTestCase2"),
                    listOf()
                ),
                TestSuiteData(
                    "testSuite2",
                    listOf("testSuite2TestCase1", "testSuite2TestCase2", "testSuite2TestCase3"),
                    listOf(),
                    listOf()
                )
            )
        )

        val highestTestSuiteIdx = runBlocking { testSuiteDatabaseRepository.fetchHighestTestSuiteIndex(publicId) }

        expectThat(highestTestSuiteIdx).isNotNull().isEqualTo(2)
    }
}
