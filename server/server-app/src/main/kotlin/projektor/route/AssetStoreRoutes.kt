package projektor.route

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveStream
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getOrFail
import projektor.asset.AssetStoreService
import projektor.asset.CreateAssetResponse
import projektor.server.api.PublicId

@KtorExperimentalAPI
fun Route.assetStore(assetStoreService: AssetStoreService?) {
    post("/run/{publicId}/asset/{assetName}") {
        val publicId = call.parameters.getOrFail("publicId")
        val assetName = call.parameters.getOrFail("assetName")
        val assetStream = call.receiveStream()

        if (assetStoreService != null) {
            assetStoreService.conditionallyCreateBucketIfNotExists()

            assetStoreService.addAsset(PublicId(publicId), assetName, assetStream)

            call.respond(HttpStatusCode.OK, CreateAssetResponse(true, true, assetName))
        } else {
            call.respond(HttpStatusCode.BadRequest, CreateAssetResponse(false, false, null))
        }
    }
}
