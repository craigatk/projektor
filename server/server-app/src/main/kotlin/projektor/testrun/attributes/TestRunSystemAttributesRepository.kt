package projektor.testrun.attributes

import projektor.server.api.PublicId
import projektor.server.api.attributes.TestRunSystemAttributes

interface TestRunSystemAttributesRepository {
    suspend fun fetchAttributes(publicId: PublicId): TestRunSystemAttributes?

    suspend fun pin(publicId: PublicId): Int

    suspend fun unpin(publicId: PublicId): Int
}
