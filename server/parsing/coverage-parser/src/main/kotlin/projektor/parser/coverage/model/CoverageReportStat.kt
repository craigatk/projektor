package projektor.parser.coverage.model

import projektor.parser.coverage.MathUtil.calculatePercentage
import java.math.BigDecimal

data class CoverageReportStat(val covered: Int, val missed: Int) {
    val total: Int
        get() = this.covered + this.missed

    val percentCovered: BigDecimal
        get() = calculatePercentage(this.covered.toBigDecimal(), this.total.toBigDecimal())
}
