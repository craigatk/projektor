package projektor.server.client

import projektor.server.api.Attachments
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ProjektorAttachmentsApi {
    @GET("/run/{publicId}/attachments")
    fun listAttachments(@Path("publicId") publicId: String): Call<Attachments>
}
