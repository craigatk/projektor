package projektor.persistence.objectstore

import net.javacrumbs.shedlock.core.LockConfiguration
import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.core.SimpleLock
import org.slf4j.LoggerFactory
import projektor.objectstore.json.JsonObjectStore
import java.net.InetAddress
import java.time.Instant
import java.util.Optional

/**
 * ShedLock lock provider that keeps each lock as a lease document, taken and released with conditional writes
 * so only one server instance can hold a lock at a time:
 *
 * - Taking a lock creates the lease document, or replaces it if the previous lease has expired. The write only
 *   succeeds if the document hasn't changed since it was read, so two instances can't both take the same lease.
 * - Unlocking shortens the lease to the lock's minimum duration, again only if nobody else has taken it since.
 *
 * Like ShedLock's JDBC provider (without its database-time option), lease times come from each instance's clock.
 */
class ObjectStoreLockProvider(private val store: JsonObjectStore) : LockProvider {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    private val lockedBy = runCatching { InetAddress.getLocalHost().hostName }.getOrDefault("unknown")

    override fun lock(lockConfiguration: LockConfiguration): Optional<SimpleLock> {
        val key = lockKey(lockConfiguration.name)
        val now = Instant.now()
        val lease = LockLeaseDocument(lockedAt = now, lockUntil = lockConfiguration.lockAtMostUntil, lockedBy = lockedBy)

        val currentLease = store.readVersioned(key, LockLeaseDocument::class.java)

        val leaseETag =
            if (currentLease == null) {
                store.create(key, lease)
            } else if (currentLease.value.lockUntil <= now) {
                store.replace(key, lease, currentLease.etag)
            } else {
                null
            }

        return if (leaseETag != null) {
            Optional.of(ObjectStoreLock(key, lockConfiguration, lease, leaseETag))
        } else {
            Optional.empty()
        }
    }

    private inner class ObjectStoreLock(
        private val key: String,
        private val lockConfiguration: LockConfiguration,
        private val lease: LockLeaseDocument,
        private val leaseETag: String,
    ) : SimpleLock {
        override fun unlock() {
            val released = store.replace(key, lease.copy(lockUntil = lockConfiguration.unlockTime), leaseETag)

            if (released == null) {
                // The lease expired and another instance took it, so there's nothing to release
                logger.warn("Lock ${lockConfiguration.name} was taken by another instance before it was released")
            }
        }
    }

    companion object {
        fun lockKey(lockName: String) = "locks/${TestRunIndex.encodeKeySegment(lockName)}.json"
    }
}

data class LockLeaseDocument(val lockedAt: Instant, val lockUntil: Instant, val lockedBy: String)
