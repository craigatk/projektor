package projektor.server.api.repository

import projektor.server.api.TestCase
import java.time.LocalDateTime

data class RepositoryFlakyTest(
    val testCase: TestCase,
    val failureCount: Int,
    val latestPublicId: String,
    val latestCreatedTimestamp: LocalDateTime
)
