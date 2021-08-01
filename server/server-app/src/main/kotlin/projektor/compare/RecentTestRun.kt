package projektor.compare

import projektor.server.api.PublicId
import java.time.Instant

data class RecentTestRun(
    val publicId: PublicId,
    val createdTimestamp: Instant,
)
