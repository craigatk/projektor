package projektor.attachment

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.ApplicationTestCaseConfig
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class AttachmentNameApplicationTest : ApplicationTestCase() {
    @Test
    fun `should not save or read an attachment whose name contains a slash`() =
        projektorTestApplication(ApplicationTestCaseConfig(attachmentsEnabled = true)) {
            val publicId = randomPublicId()

            val postResponse = client.post("/run/$publicId/attachments/nested%2Ffile.txt") { setBody("contents") }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.BadRequest)

            val getResponse = client.get("/run/$publicId/attachments/nested%2Ffile.txt")
            expectThat(getResponse.status).isEqualTo(HttpStatusCode.NotFound)
        }

    @Test
    fun `should reject a public ID that isn't one when adding an attachment`() =
        projektorTestApplication(ApplicationTestCaseConfig(attachmentsEnabled = true)) {
            val response = client.post("/run/created%2F2020/attachments/01-01%2Fx.json") { setBody("{}") }

            expectThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        }
}
