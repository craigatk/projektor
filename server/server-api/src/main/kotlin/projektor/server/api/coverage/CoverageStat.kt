package projektor.server.api.coverage

import projektor.server.api.MathUtil.calculatePercentage
import java.math.BigDecimal

data class CoverageStat(
    val covered: Int,
    val missed: Int,
    val coveredPercentageDelta: BigDecimal?
) {
    var total: Int = 0
        get() = covered + missed

    var coveredPercentage: BigDecimal = BigDecimal.ZERO
        get() = calculatePercentage(this.covered.toBigDecimal(), this.total.toBigDecimal())
}
