package projektor.incomingresults.mapper

import projektor.server.api.PublicId
import projektor.server.api.TestRunSummary
import projektor.server.api.util.calculateAverageDuration
import java.math.BigDecimal
import java.time.Instant
import projektor.parser.model.TestSuite as ParsedTestSuite
import projektor.server.api.TestSuite as TestSuiteApi

fun toTestRunSummary(
    publicId: PublicId,
    testSuites: List<ParsedTestSuite>,
    wallClockDuration: BigDecimal?
): TestRunSummary {
    val totalTestCount = testSuites.sumOf { it.tests }
    val totalFailureCount = testSuites.sumOf { it.failures }

    val allTestCases = testSuites.flatMap { it.testCases }
    val cumulativeDuration = allTestCases
        .mapNotNull { it.time }
        .fold(BigDecimal.ZERO, BigDecimal::add)
    val averageDuration = calculateAverageDuration(cumulativeDuration, totalTestCount)
    val slowestTestCaseDuration = allTestCases
        .mapNotNull { it.time }
        .maxOrNull()
        ?: BigDecimal.ZERO

    return TestRunSummary(
        publicId.id,
        totalTestCount,
        testSuites.sumOf { it.passingCount },
        testSuites.sumOf { it.skipped },
        totalFailureCount,
        totalFailureCount == 0,
        cumulativeDuration,
        averageDuration,
        slowestTestCaseDuration,
        Instant.now(),
        wallClockDuration
    )
}

fun toTestRunSummaryFromApi(
    publicId: PublicId,
    testSuites: List<TestSuiteApi>,
    wallClockDuration: BigDecimal?
): TestRunSummary {
    val totalTestCount = testSuites.sumOf { it.testCount }
    val totalFailureCount = testSuites.sumOf { it.failureCount }

    val allTestCases = testSuites.mapNotNull { it.testCases }.flatten()
    val cumulativeDuration = allTestCases
        .mapNotNull { it.duration }
        .fold(BigDecimal.ZERO, BigDecimal::add)
    val averageDuration = calculateAverageDuration(cumulativeDuration, totalTestCount)
    val slowestTestCaseDuration = allTestCases
        .mapNotNull { it.duration }
        .maxOrNull()
        ?: BigDecimal.ZERO

    return TestRunSummary(
        publicId.id,
        totalTestCount,
        testSuites.sumOf { it.passingCount },
        testSuites.sumOf { it.skippedCount },
        totalFailureCount,
        totalFailureCount == 0,
        cumulativeDuration,
        averageDuration,
        slowestTestCaseDuration,
        Instant.now(),
        wallClockDuration
    )
}
