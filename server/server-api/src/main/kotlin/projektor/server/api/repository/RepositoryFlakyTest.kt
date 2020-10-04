package projektor.server.api.repository

import projektor.server.api.TestCase
import java.math.BigDecimal
import java.time.LocalDateTime

data class RepositoryFlakyTest(
    val testCase: TestCase,
    val failureCount: Int,
    val failurePercentage: BigDecimal,
    val latestPublicId: String,
    val latestCreatedTimestamp: LocalDateTime
)
