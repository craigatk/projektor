package projektor.server.client

import projektor.server.api.TestRun
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ProjektorResultsApi {
    @GET("/run/{publicId}")
    fun testRun(@Path("publicId") publicId: String): Call<TestRun>
}
