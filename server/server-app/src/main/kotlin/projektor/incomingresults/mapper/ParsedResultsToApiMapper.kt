package projektor.incomingresults.mapper

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.LocalDateTime
import projektor.parser.model.TestSuite
import projektor.server.api.PublicId
import projektor.server.api.TestRunSummary

val roundingMathContext = MathContext(3, RoundingMode.HALF_UP)

fun toTestRunSummary(publicId: PublicId, testSuites: List<TestSuite>): TestRunSummary {
    val totalTestCount = testSuites.sumBy { it.tests }
    val totalFailureCount = testSuites.sumBy { it.failures }

    val allTestCases = testSuites.flatMap { it.testCases }
    val cumulativeDuration = allTestCases
            .mapNotNull { it.time }
            .fold(BigDecimal.ZERO, BigDecimal::add)
    val averageDuration = cumulativeDuration.divide(totalTestCount.toBigDecimal(), roundingMathContext)
    val slowestTestCaseDuration = allTestCases
            .mapNotNull { it.time }
            .max()
            ?: BigDecimal.ZERO

    return TestRunSummary(
            publicId.id,
            totalTestCount,
            testSuites.sumBy { it.passingCount },
            testSuites.sumBy { it.skipped },
            totalFailureCount,
            totalFailureCount == 0,
            cumulativeDuration,
            averageDuration,
            slowestTestCaseDuration,
            LocalDateTime.now()
    )
}
