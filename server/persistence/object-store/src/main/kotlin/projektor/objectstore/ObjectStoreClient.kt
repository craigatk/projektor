package projektor.objectstore

import io.minio.BucketExistsArgs
import io.minio.GetObjectArgs
import io.minio.Http
import io.minio.ListObjectsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import io.minio.RemoveObjectsArgs
import io.minio.StatObjectArgs
import io.minio.errors.ErrorResponseException
import io.minio.messages.DeleteRequest
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import projektor.objectstore.bucket.BucketCreationException
import java.io.InputStream
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit

open class ObjectStoreClient internal constructor(
    config: ObjectStoreConfig,
    internal val telemetry: ObjectStoreTelemetry,
    httpClient: OkHttpClient,
) {
    constructor(
        config: ObjectStoreConfig,
        telemetry: ObjectStoreTelemetry = ObjectStoreTelemetry(),
    ) : this(config, telemetry, createHttpClient())

    private val minioClient =
        MinioClient.builder()
            .endpoint(config.url)
            .credentials(config.accessKey, config.secretKey)
            .httpClient(httpClient)
            .build()

    fun createBucketIfNotExists(bucketName: String) {
        try {
            if (!bucketExists(bucketName)) {
                telemetry.record("createBucket", bucketName, null) {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build())
                }
            }
        } catch (e: ErrorResponseException) {
            throw BucketCreationException("Error creating bucket", e)
        }
    }

    fun bucketExists(bucketName: String): Boolean =
        telemetry.record("bucketExists", bucketName, null) {
            minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build(),
            )
        }

    fun putObject(
        bucketName: String,
        objectName: String,
        stream: InputStream,
    ) {
        telemetry.record("put", bucketName, objectName) {
            minioClient.putObject(
                PutObjectArgs
                    .builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .stream(stream, -1, 10485760)
                    .contentType("application/octet-stream")
                    .build(),
            )
        }
    }

    /**
     * Writes the object in a single request, optionally only if the given condition holds.
     * Returns the new object's ETag.
     *
     * The HTTP client retries a request that failed with a retryable status, even if the store had committed it. The
     * retry then fails its condition against this write's own object, which would look like a conflict with another
     * writer and make callers apply their change twice. So conditional writes carry a unique ID in the object's
     * metadata, and a failed condition only counts as a conflict if the object doesn't have this write's ID.
     *
     * @throws PreconditionFailedException if the condition didn't hold
     */
    open fun putObject(
        bucketName: String,
        objectName: String,
        bytes: ByteArray,
        contentType: String,
        condition: WriteCondition = WriteCondition.None,
    ): String {
        val conditionHeaders =
            when (condition) {
                WriteCondition.None -> mapOf()
                WriteCondition.IfNotExists -> mapOf("If-None-Match" to "*")
                is WriteCondition.IfMatch -> mapOf("If-Match" to quoteETag(condition.etag))
            }
        val writeId = UUID.randomUUID().toString()
        val userMetadata = if (condition == WriteCondition.None) mapOf() else mapOf(WRITE_ID_METADATA to writeId)

        return telemetry.record("put", bucketName, objectName) {
            try {
                val response =
                    minioClient.putObject(
                        PutObjectArgs
                            .builder()
                            .bucket(bucketName)
                            .`object`(objectName)
                            .data(bytes, bytes.size)
                            .contentType(contentType)
                            .headers(conditionHeaders)
                            .userMetadata(userMetadata)
                            .build(),
                    )

                normalizeETag(response.etag())
            } catch (e: ErrorResponseException) {
                if (isPreconditionFailure(e)) {
                    eTagIfWrittenBy(writeId, bucketName, objectName)
                        ?: throw PreconditionFailedException(bucketName, objectName, condition, e)
                } else {
                    throw e
                }
            }
        }
    }

    fun getObject(
        bucketName: String,
        objectName: String,
    ): InputStream? =
        // Only times the request up to the response headers, the caller reads the body from the returned stream
        telemetry.record("get", bucketName, objectName, ::outcomeOfGet) {
            try {
                minioClient.getObject(
                    GetObjectArgs
                        .builder()
                        .bucket(bucketName)
                        .`object`(objectName)
                        .build(),
                )
            } catch (e: ErrorResponseException) {
                null
            }
        }

    /**
     * Reads the full object contents along with its ETag, or returns null if the object doesn't exist.
     */
    open fun getObjectWithETag(
        bucketName: String,
        objectName: String,
    ): StoredObject? =
        telemetry.record("get", bucketName, objectName, ::outcomeOfGet) {
            try {
                minioClient.getObject(
                    GetObjectArgs
                        .builder()
                        .bucket(bucketName)
                        .`object`(objectName)
                        .build(),
                ).use { response ->
                    StoredObject(
                        bytes = response.readBytes(),
                        etag = requireETag(response.headers()["ETag"], bucketName, objectName),
                    )
                }
            } catch (e: ErrorResponseException) {
                if (e.errorResponse().code() == "NoSuchKey") {
                    null
                } else {
                    throw e
                }
            }
        }

    /**
     * Whether the object exists, without reading its contents.
     */
    open fun objectExists(
        bucketName: String,
        objectName: String,
    ): Boolean =
        telemetry.record("stat", bucketName, objectName) {
            try {
                minioClient.statObject(
                    StatObjectArgs
                        .builder()
                        .bucket(bucketName)
                        .`object`(objectName)
                        .build(),
                )
                true
            } catch (e: ErrorResponseException) {
                if (e.errorResponse().code() == "NoSuchKey") {
                    false
                } else {
                    throw e
                }
            }
        }

    /**
     * When the object was last written, or null if it doesn't exist.
     */
    fun objectLastModified(
        bucketName: String,
        objectName: String,
    ): Instant? =
        telemetry.record("stat", bucketName, objectName) {
            try {
                minioClient.statObject(
                    StatObjectArgs
                        .builder()
                        .bucket(bucketName)
                        .`object`(objectName)
                        .build(),
                ).lastModified().toInstant()
            } catch (e: ErrorResponseException) {
                if (e.errorResponse().code() == "NoSuchKey") {
                    null
                } else {
                    throw e
                }
            }
        }

    /**
     * Lists the object names (full keys) under the prefix in lexicographic order.
     *
     * @param delimiter when set, keys containing the delimiter after the prefix are rolled up into
     * ObjectListing.commonPrefixes instead of being returned individually.
     * @param startAfter only return keys that sort after this key
     * @param limit the maximum number of entries (object names plus common prefixes) to return
     */
    fun listObjects(
        bucketName: String,
        prefix: String,
        delimiter: String? = null,
        startAfter: String? = null,
        limit: Int? = null,
    ): ObjectListing {
        val argsBuilder =
            ListObjectsArgs
                .builder()
                .bucket(bucketName)
                .prefix(prefix)
                .recursive(delimiter == null)

        if (delimiter != null) {
            argsBuilder.delimiter(delimiter)
        }
        if (startAfter != null) {
            argsBuilder.startAfter(startAfter)
        }
        if (limit != null) {
            argsBuilder.maxKeys(minOf(limit, MAX_KEYS_PER_PAGE))
        }

        // The iterable fetches further pages lazily as it's consumed, so taking the limit avoids fetching extra pages.
        // Recorded as one operation that covers every page it fetched.
        val items =
            telemetry.record("list", bucketName, prefix) {
                minioClient.listObjects(argsBuilder.build())
                    .asSequence()
                    .map { it.get() }
                    .let { if (limit != null) it.take(limit) else it }
                    .toList()
            }

        val (prefixItems, objectItems) = items.partition { it.isDir }

        return ObjectListing(
            objectNames = objectItems.map { it.objectName() },
            commonPrefixes = prefixItems.map { it.objectName() },
        )
    }

    fun removeObject(
        bucketName: String,
        objectName: String,
    ) {
        telemetry.record("delete", bucketName, objectName) {
            minioClient.removeObject(
                RemoveObjectArgs
                    .builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .build(),
            )
        }
    }

    open fun removeObjects(
        bucketName: String,
        objectNames: List<String>,
    ) {
        // A single delete request can contain at most 1000 objects
        objectNames.chunked(MAX_KEYS_PER_PAGE).forEach { objectNamesChunk ->
            val objects = objectNamesChunk.map { DeleteRequest.Object(it) }
            telemetry.record("deleteBatch", bucketName, null) {
                val results =
                    minioClient.removeObjects(
                        RemoveObjectsArgs
                            .builder()
                            .bucket(bucketName)
                            .objects(objects)
                            .build(),
                    )
                // removeObjects only sends the delete requests as this iterable is consumed. The iterable yields an entry
                // for each object that failed to delete, and get() only throws if the whole request failed.
                val failures = results.map { it.get() }.filter { it.code() != "NoSuchKey" }

                if (failures.isNotEmpty()) {
                    throw ObjectDeletionException(bucketName, failures.map { "${it.objectName()}: ${it.code()}" })
                }
            }
        }
    }

    fun removeObjectsWithPrefix(
        bucketName: String,
        prefix: String,
    ) {
        val objectNames = listObjects(bucketName, prefix).objectNames

        if (objectNames.isNotEmpty()) {
            removeObjects(bucketName, objectNames)
        }
    }

    private fun eTagIfWrittenBy(
        writeId: String,
        bucketName: String,
        objectName: String,
    ): String? =
        try {
            minioClient.statObject(
                StatObjectArgs
                    .builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .build(),
            ).takeIf { it.userMetadata().getFirst(WRITE_ID_METADATA) == writeId }?.let { normalizeETag(it.etag()) }
        } catch (e: ErrorResponseException) {
            if (e.errorResponse().code() == "NoSuchKey") {
                null
            } else {
                throw e
            }
        }

    companion object {
        private const val MAX_KEYS_PER_PAGE = 1000

        private const val WRITE_ID_METADATA = "projektor-write-id"

        // Requests beyond this wait in a queue. Matches the threads of Dispatchers.IO, which all calls are made from.
        internal const val MAX_CONCURRENT_REQUESTS = 64

        private const val CONNECT_TIMEOUT_SECONDS = 10L

        // Between individual reads and writes on the connection, so these don't limit how long a large object can take
        private const val READ_WRITE_TIMEOUT_SECONDS = 60L

        /**
         * MinIO's default client, which retries requests that failed with a retryable status, with two changes. MinIO
         * sends requests through OkHttp's dispatcher, whose default of 5 concurrent requests per host would limit
         * this whole client to 5 requests at a time. And a stalled connection fails after a minute, not MinIO's 5.
         */
        internal fun createHttpClient(): OkHttpClient =
            Http.newDefaultClient()
                .newBuilder()
                .dispatcher(
                    Dispatcher().apply {
                        maxRequests = MAX_CONCURRENT_REQUESTS
                        maxRequestsPerHost = MAX_CONCURRENT_REQUESTS
                    },
                )
                .connectionPool(ConnectionPool(MAX_CONCURRENT_REQUESTS, 5, TimeUnit.MINUTES))
                .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(READ_WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(READ_WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build()

        private fun outcomeOfGet(result: Any?) = if (result == null) ObjectStoreTelemetry.NOT_FOUND else ObjectStoreTelemetry.SUCCESS

        // S3 returns 412 when the condition doesn't hold, and AWS can return 409 when a conflicting
        // conditional write to the same key is in flight - both mean the caller should re-read and retry.
        private fun isPreconditionFailure(e: ErrorResponseException) =
            e.response()?.code in listOf(409, 412) ||
                e.errorResponse().code() in listOf("PreconditionFailed", "ConditionalRequestConflict")

        // ETags come back from S3 wrapped in double quotes, normalize to the bare value
        internal fun normalizeETag(etag: String) = etag.trim().trim('"')

        /**
         * Conditional writes need the ETag the object was read with. Without one (e.g. a proxy stripped the header),
         * every If-Match write would fail and look like endless concurrent updates, so fail clearly instead.
         */
        internal fun requireETag(
            etag: String?,
            bucketName: String,
            objectName: String,
        ): String =
            etag?.let { normalizeETag(it) }?.takeIf { it.isNotEmpty() }
                ?: throw MissingETagException(bucketName, objectName)

        private fun quoteETag(etag: String) = "\"${normalizeETag(etag)}\""
    }
}

sealed class WriteCondition {
    data object None : WriteCondition()

    data object IfNotExists : WriteCondition()

    data class IfMatch(val etag: String) : WriteCondition()
}

class StoredObject(val bytes: ByteArray, val etag: String)

data class ObjectListing(val objectNames: List<String>, val commonPrefixes: List<String>)

class PreconditionFailedException(
    bucketName: String,
    objectName: String,
    condition: WriteCondition,
    cause: Throwable,
) : RuntimeException("Write condition $condition failed for object $objectName in bucket $bucketName", cause)

class ObjectDeletionException(
    bucketName: String,
    failures: List<String>,
) : RuntimeException("Failed to delete ${failures.size} objects from bucket $bucketName, including ${failures.take(5)}")

class MissingETagException(
    bucketName: String,
    objectName: String,
) : RuntimeException(
        "Object $objectName in bucket $bucketName was returned without an ETag, which conditional writes need. " +
            "Check that the object store (or a proxy in front of it) returns the ETag header.",
    )
