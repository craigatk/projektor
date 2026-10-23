package projektor.objectstore.json

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import projektor.objectstore.ObjectListing
import projektor.objectstore.ObjectStoreClient
import projektor.objectstore.PreconditionFailedException
import projektor.objectstore.WriteCondition
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Stores JSON documents in a single bucket, with all keys under an optional key prefix.
 * Keys passed to and returned from this class are relative to that prefix.
 *
 * Documents larger than the compression threshold are gzipped. Reads detect gzipped documents
 * by their magic bytes, so the threshold can change without affecting existing documents.
 *
 * All calls are blocking, callers should run them on an IO dispatcher.
 */
class JsonObjectStore(
    private val client: ObjectStoreClient,
    private val bucketName: String,
    keyPrefix: String = "",
    private val objectMapper: ObjectMapper = createObjectMapper(),
    private val compressionThresholdBytes: Int = DEFAULT_COMPRESSION_THRESHOLD_BYTES,
    private val maxUpdateAttempts: Int = DEFAULT_MAX_UPDATE_ATTEMPTS,
) {
    private val keyPrefix = normalizeKeyPrefix(keyPrefix)

    fun <T> read(
        key: String,
        type: Class<T>,
    ): T? = readVersioned(key, type)?.value

    inline fun <reified T> read(key: String): T? = read(key, T::class.java)

    fun <T> readVersioned(
        key: String,
        type: Class<T>,
    ): VersionedDocument<T>? =
        client.getObjectWithETag(bucketName, fullKey(key))?.let { storedObject ->
            VersionedDocument(objectMapper.readValue(decode(storedObject.bytes), type), storedObject.etag)
        }

    /**
     * Writes the document unconditionally, replacing any existing document at the key.
     * Returns the new document's ETag.
     */
    fun write(
        key: String,
        value: Any,
    ): String = put(key, value, WriteCondition.None)

    /**
     * Writes the document only if no document exists yet at the key.
     * Returns the new document's ETag, or null if a document already existed.
     */
    fun create(
        key: String,
        value: Any,
    ): String? =
        try {
            put(key, value, WriteCondition.IfNotExists)
        } catch (e: PreconditionFailedException) {
            null
        }

    /**
     * Replaces the document only if it hasn't changed since it was read with the given ETag.
     * Returns the new document's ETag, or null if the document had changed.
     */
    fun replace(
        key: String,
        value: Any,
        etag: String,
    ): String? =
        try {
            put(key, value, WriteCondition.IfMatch(etag))
        } catch (e: PreconditionFailedException) {
            null
        }

    /**
     * Atomically read-modify-writes the document at the key. The transform receives the current
     * document (null if there isn't one yet) and returns the new document, or null to leave the stored
     * document unchanged. If another writer changes the document in between, the transform runs again
     * with the latest document, so it must not have side effects.
     *
     * Returns the document as stored after the update (the current document if the transform returned null).
     *
     * @throws ConcurrentUpdateException if the update kept conflicting with other writers
     */
    fun <T : Any> update(
        key: String,
        type: Class<T>,
        transform: (T?) -> T?,
    ): T? {
        repeat(maxUpdateAttempts) { attempt ->
            val current = readVersioned(key, type)
            val updated = transform(current?.value) ?: return current?.value

            val newETag =
                if (current == null) {
                    create(key, updated)
                } else {
                    replace(key, updated, current.etag)
                }

            if (newETag != null) {
                return updated
            }

            client.telemetry.recordUpdateConflict(bucketName)
            backoff(attempt)
        }

        client.telemetry.recordUpdateExhausted(bucketName)
        throw ConcurrentUpdateException(key, maxUpdateAttempts)
    }

    inline fun <reified T : Any> update(
        key: String,
        noinline transform: (T?) -> T?,
    ): T? = update(key, T::class.java, transform)

    fun delete(key: String) {
        client.removeObject(bucketName, fullKey(key))
    }

    /**
     * Checks that the object store honors conditional writes, which every compare-and-swap update relies on.
     * A store that ignores If-None-Match or If-Match accepts every write, which silently loses concurrent updates
     * instead of failing.
     *
     * @throws ConditionalWritesNotSupportedException if the store didn't honor a write condition
     */
    fun verifyConditionalWrites() {
        val key = "$CONDITIONAL_WRITE_PROBE_PREFIX${UUID.randomUUID()}.json"

        fun notSupported(reason: String): Nothing = throw ConditionalWritesNotSupportedException(bucketName, reason)

        try {
            create(key, ConditionalWriteProbeDocument(1)) ?: notSupported("creating a new object with If-None-Match: * was rejected")

            if (create(key, ConditionalWriteProbeDocument(2)) != null) {
                notSupported("a write with If-None-Match: * replaced an existing object")
            }

            val current =
                readVersioned(key, ConditionalWriteProbeDocument::class.java)
                    ?: notSupported("the object written by the check couldn't be read back")

            if (replace(key, ConditionalWriteProbeDocument(3), STALE_PROBE_ETAG) != null) {
                notSupported("a write with If-Match and a stale ETag replaced the object")
            }

            replace(key, ConditionalWriteProbeDocument(4), current.etag)
                ?: notSupported("a write with If-Match and the object's current ETag was rejected")
        } finally {
            runCatching { delete(key) }
        }
    }

    fun deletePrefix(prefix: String) {
        client.removeObjectsWithPrefix(bucketName, fullKey(prefix))
    }

    /**
     * Lists keys under the prefix in lexicographic order. See ObjectStoreClient.listObjects for the parameters.
     */
    fun list(
        prefix: String,
        delimiter: String? = null,
        startAfter: String? = null,
        limit: Int? = null,
    ): ObjectListing {
        val listing = client.listObjects(bucketName, fullKey(prefix), delimiter, startAfter?.let { fullKey(it) }, limit)

        return ObjectListing(
            objectNames = listing.objectNames.map { relativeKey(it) },
            commonPrefixes = listing.commonPrefixes.map { relativeKey(it) },
        )
    }

    private fun put(
        key: String,
        value: Any,
        condition: WriteCondition,
    ): String {
        val json = objectMapper.writeValueAsBytes(value)

        return if (json.size > compressionThresholdBytes) {
            client.putObject(bucketName, fullKey(key), gzip(json), GZIP_CONTENT_TYPE, condition)
        } else {
            client.putObject(bucketName, fullKey(key), json, JSON_CONTENT_TYPE, condition)
        }
    }

    private fun fullKey(key: String) = keyPrefix + key

    private fun relativeKey(fullKey: String) = fullKey.removePrefix(keyPrefix)

    private fun backoff(attempt: Int) {
        val maxDelayMillis = minOf(MAX_BACKOFF_MILLIS, BASE_BACKOFF_MILLIS shl minOf(attempt, 10))
        Thread.sleep(ThreadLocalRandom.current().nextLong(maxDelayMillis + 1))
    }

    companion object {
        const val DEFAULT_COMPRESSION_THRESHOLD_BYTES = 8 * 1024
        const val DEFAULT_MAX_UPDATE_ATTEMPTS = 20

        private const val BASE_BACKOFF_MILLIS = 10L
        private const val MAX_BACKOFF_MILLIS = 500L

        private const val CONDITIONAL_WRITE_PROBE_PREFIX = "conditional-write-check/"

        // A well-formed ETag that never matches a real object
        private const val STALE_PROBE_ETAG = "00000000000000000000000000000000"

        private const val JSON_CONTENT_TYPE = "application/json"
        private const val GZIP_CONTENT_TYPE = "application/gzip"

        fun createObjectMapper(): ObjectMapper =
            jacksonObjectMapper()
                .registerModule(JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

        internal fun normalizeKeyPrefix(keyPrefix: String): String {
            val trimmed = keyPrefix.trim('/')
            return if (trimmed.isEmpty()) "" else "$trimmed/"
        }

        internal fun gzip(bytes: ByteArray): ByteArray {
            val outputStream = ByteArrayOutputStream()
            GZIPOutputStream(outputStream).use { it.write(bytes) }
            return outputStream.toByteArray()
        }

        internal fun decode(bytes: ByteArray): ByteArray =
            if (isGzipped(bytes)) {
                GZIPInputStream(ByteArrayInputStream(bytes)).use { it.readBytes() }
            } else {
                bytes
            }

        // JSON documents never start with the gzip magic bytes, so this is unambiguous
        private fun isGzipped(bytes: ByteArray) = bytes.size >= 2 && bytes[0] == 0x1f.toByte() && bytes[1] == 0x8b.toByte()
    }
}

data class VersionedDocument<T>(val value: T, val etag: String)

class ConcurrentUpdateException(key: String, attempts: Int) :
    RuntimeException("Gave up updating $key after $attempts attempts due to concurrent updates")

class ConditionalWritesNotSupportedException(bucketName: String, reason: String) :
    RuntimeException(
        "Object store bucket $bucketName doesn't support conditional writes ($reason). " +
            "Object-store persistence needs a store that honors If-None-Match and If-Match on PutObject, such as AWS S3 or MinIO.",
    )

internal data class ConditionalWriteProbeDocument(val value: Int)
