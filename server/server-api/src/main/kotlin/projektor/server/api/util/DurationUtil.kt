package projektor.server.api.util

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

private val roundingMathContext = MathContext(3, RoundingMode.HALF_UP)

fun calculateAverageDuration(
    cumulativeDuration: BigDecimal,
    totalTestCount: Int,
): BigDecimal {
    return if (cumulativeDuration > BigDecimal.ZERO && totalTestCount > 0) {
        cumulativeDuration.divide(totalTestCount.toBigDecimal(), roundingMathContext)
    } else {
        BigDecimal.ZERO
    }
}
