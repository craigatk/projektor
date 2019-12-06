package projektor.server.api

import java.math.BigDecimal

data class TestCase(
    val idx: Int,
    val testSuiteIdx: Int,
    val name: String,
    val packageName: String?,
    val className: String,
    val duration: BigDecimal,
    val passed: Boolean,
    val skipped: Boolean,
    val hasSystemOut: Boolean,
    val hasSystemErr: Boolean,
    val failure: TestFailure?
)
