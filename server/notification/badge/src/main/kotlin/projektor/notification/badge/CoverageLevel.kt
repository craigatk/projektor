package projektor.notification.badge

import java.math.BigDecimal

enum class CoverageLevel(val fillColor: String) {
    GOOD("#4c1"),
    OK("#dfb317"),
    POOR("#fe7d37"),
    TERRIBLE("#e05d44"),
    ;

    companion object {
        fun of(coveredPercentage: BigDecimal): CoverageLevel =
            when {
                coveredPercentage >= BigDecimal("90.00") -> GOOD
                coveredPercentage >= BigDecimal("80.00") -> OK
                coveredPercentage >= BigDecimal("70.00") -> POOR
                else -> TERRIBLE
            }
    }
}
