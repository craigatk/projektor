package projektor.parser.coverage

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object MathUtil {
    fun calculatePercentage(
        value: BigDecimal,
        total: BigDecimal,
    ): BigDecimal =
        if (value > BigDecimal.ZERO && total > BigDecimal.ZERO) {
            value.divide(total, MathContext(4, RoundingMode.HALF_DOWN))
                .times(BigDecimal(100.00)).setScale(2, RoundingMode.HALF_DOWN)
        } else {
            BigDecimal.ZERO
        }
}
