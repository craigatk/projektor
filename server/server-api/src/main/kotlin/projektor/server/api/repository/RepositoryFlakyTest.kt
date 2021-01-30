package projektor.server.api.repository

import projektor.server.api.TestCase
import java.math.BigDecimal

data class RepositoryFlakyTest(
    val testCase: TestCase,
    val failureCount: Int,
    val failurePercentage: BigDecimal,
    val firstTestCase: TestCase,
    val latestTestCase: TestCase,
)
