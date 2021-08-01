package projektor.server.api.repository.coverage

import java.math.BigDecimal
import java.time.Instant

data class RepositoryCurrentCoverage(
    val id: String,
    val createdTimestamp: Instant,
    val coveredPercentage: BigDecimal
)
