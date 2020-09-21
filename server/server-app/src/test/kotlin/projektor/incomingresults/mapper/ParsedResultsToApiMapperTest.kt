package projektor.incomingresults.mapper

import java.math.BigDecimal
import org.junit.jupiter.api.Test
import projektor.incomingresults.randomPublicId
import projektor.parser.model.TestCase
import projektor.parser.model.TestSuite
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ParsedResultsToApiMapperTest {
    @Test
    fun `should be able to parse empty test suite list`() {
        val publicId = randomPublicId()

        val testRunSummary = toTestRunSummary(publicId, listOf(), null)

        expectThat(testRunSummary).and {
            get { totalTestCount }.isEqualTo(0)
            get { totalPassingCount }.isEqualTo(0)
            get { totalFailureCount }.isEqualTo(0)
            get { totalSkippedCount }.isEqualTo(0)
            get { passed }.isEqualTo(true)
            get { averageDuration }.isEqualTo(BigDecimal.ZERO)
            get { cumulativeDuration }.isEqualTo(BigDecimal.ZERO)
            get { slowestTestCaseDuration }.isEqualTo(BigDecimal.ZERO)
        }
    }

    @Test
    fun `should be able to map test suites that don't have duration values set`() {
        val publicId = randomPublicId()

        val testSuite1 = TestSuite()
        testSuite1.time = BigDecimal.ZERO
        testSuite1.testCases = listOf(TestCase())
        val testSuite2 = TestSuite()
        testSuite2.time = BigDecimal.ZERO
        testSuite2.testCases = listOf(TestCase())

        val testRunSummary = toTestRunSummary(publicId, listOf(testSuite1, testSuite2), null)

        expectThat(testRunSummary).and {
            get { averageDuration }.isEqualTo(BigDecimal.ZERO)
            get { cumulativeDuration }.isEqualTo(BigDecimal.ZERO)
            get { slowestTestCaseDuration }.isEqualTo(BigDecimal.ZERO)
        }
    }
}
