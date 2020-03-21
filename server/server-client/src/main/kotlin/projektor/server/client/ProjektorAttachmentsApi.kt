package projektor.server.client

import okhttp3.ResponseBody
import projektor.server.api.attachments.Attachments
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ProjektorAttachmentsApi {
    @GET("/run/{publicId}/attachments")
    fun listAttachments(@Path("publicId") publicId: String): Call<Attachments>

    @GET("/run/{publicId}/attachments/{fileName}")
    fun getAttachments(@Path("publicId") publicId: String, @Path("fileName") fileName: String): Call<ResponseBody>
}
