package projektor.server.client

import projektor.server.api.TestRun
import projektor.server.api.TestSuite
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ProjektorTestRunApi {
    @GET("/run/{publicId}")
    fun testRun(@Path("publicId") publicId: String): Call<TestRun>

    @GET("/run/{publicId}/suites")
    fun testSuites(@Path("publicId") publicId: String): Call<List<TestSuite>>

    @GET("/run/{publicId}/suite/{testSuiteIdx}")
    fun testSuite(@Path("publicId") publicId: String, @Path("testSuiteIdx") testRunIndex: Int): Call<TestSuite>
}
