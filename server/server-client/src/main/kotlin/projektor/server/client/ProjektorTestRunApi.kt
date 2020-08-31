package projektor.server.client

import projektor.server.api.PublicId
import projektor.server.api.TestRun
import projektor.server.api.TestSuite
import projektor.server.api.TestSuiteOutput
import projektor.server.api.coverage.CoverageExists
import projektor.server.api.coverage.CoverageStats
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ProjektorTestRunApi {
    @GET("/run/{publicId}")
    fun testRun(@Path("publicId") publicId: String): Call<TestRun>

    @GET("/run/{publicId}/suites")
    fun testSuites(@Path("publicId") publicId: String): Call<List<TestSuite>>

    @GET("/run/{publicId}/suite/{testSuiteIdx}")
    fun testSuite(@Path("publicId") publicId: String, @Path("testSuiteIdx") testSuiteIdx: Int): Call<TestSuite>

    @GET("/run/{publicId}/suite/{testSuiteIdx}/systemOut")
    fun testSuiteSystemOut(@Path("publicId") publicId: String, @Path("testSuiteIdx") testSuiteIdx: Int): Call<TestSuiteOutput>

    @GET("/run/{publicId}/suite/{testSuiteIdx}/systemErr")
    fun testSuiteSystemErr(@Path("publicId") publicId: String, @Path("testSuiteIdx") testSuiteIdx: Int): Call<TestSuiteOutput>

    @GET("/run/{publicId}/coverage/overall")
    fun coverageOverallStats(@Path("publicId") publicId: String): Call<CoverageStats>

    @GET("/run/{publicId}/coverage/exists")
    fun coverageExists(@Path("publicId") publicId: String): Call<CoverageExists>

    @GET("/run/{publicId}/previous")
    fun previousTestRun(@Path("publicId") publicId: String): Call<PublicId>
}
