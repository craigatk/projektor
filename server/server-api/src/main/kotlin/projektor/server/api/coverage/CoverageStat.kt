package projektor.server.api.coverage

import java.math.BigDecimal

data class CoverageStat(
        val covered: Int,
        val missed: Int,
        val total: Int,
        val coveredPercentage: BigDecimal
)
