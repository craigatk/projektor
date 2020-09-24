package projektor.server.client

import projektor.server.api.metadata.TestRunMetadata
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ProjektorTestRunMetadataApi {
    @GET("/run/{publicId}/metadata")
    fun testRunMetadata(@Path("publicId") publicId: String): Call<TestRunMetadata>
}
