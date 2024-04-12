package projektor.incomingresults

import projektor.server.api.PublicId

data class PersistTestResultsException(val publicId: PublicId, val errorMessage: String, override val cause: Throwable) : RuntimeException(
    errorMessage,
    cause,
)
