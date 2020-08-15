package projektor.parser.coverage.model

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

data class CoverageReportStat(val covered: Int, val missed: Int) {
    val total: Int
        get() = this.covered + this.missed

    val percentCovered: BigDecimal
        get() = (this.covered.toBigDecimal())
                    .divide(this.total.toBigDecimal(), MathContext(4, RoundingMode.HALF_DOWN))
                .times(BigDecimal(100.00)).setScale(2, RoundingMode.HALF_DOWN)
}
