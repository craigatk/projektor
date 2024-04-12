package projektor.server.api

import java.math.BigDecimal
import java.time.LocalDateTime

data class TestSuite(
    val idx: Int,
    val fileName: String?,
    val packageName: String?,
    val className: String,
    val testCount: Int,
    val passingCount: Int,
    val skippedCount: Int,
    val failureCount: Int,
    val startTs: LocalDateTime?,
    val hostname: String?,
    val duration: BigDecimal,
    val hasSystemOut: Boolean,
    val hasSystemErr: Boolean,
    val groupName: String?,
    val groupLabel: String?,
    val testCases: List<TestCase>?,
)
