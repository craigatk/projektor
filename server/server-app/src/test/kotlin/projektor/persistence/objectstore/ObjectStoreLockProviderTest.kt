package projektor.persistence.objectstore

import net.javacrumbs.shedlock.core.LockConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import projektor.objectstore.ObjectStoreClient
import projektor.objectstore.ObjectStoreConfig
import projektor.objectstore.json.JsonObjectStore
import projektor.schedule.ScheduleDelay
import projektor.schedule.ScheduledJob
import projektor.schedule.Scheduler
import projektor.schedule.SchedulerLock
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class ObjectStoreLockProviderTest {
    private val objectStoreClient = ObjectStoreClient(ObjectStoreConfig("http://localhost:9000", "minio_access_key", "minio_secret_key"))
    private lateinit var store: JsonObjectStore
    private lateinit var lockProvider: ObjectStoreLockProvider

    @BeforeEach
    fun createStore() {
        objectStoreClient.createBucketIfNotExists(BUCKET_NAME)
        store = JsonObjectStore(objectStoreClient, BUCKET_NAME, "locks-${UUID.randomUUID()}")
        lockProvider = ObjectStoreLockProvider(store)
    }

    @Test
    fun `should not lock while another lock holds the lease`() {
        val lock = lockProvider.lock(lockConfiguration("job", lockAtMostFor = Duration.ofMinutes(5)))
        expectThat(lock.isPresent).isTrue()

        expectThat(lockProvider.lock(lockConfiguration("job", lockAtMostFor = Duration.ofMinutes(5))).isPresent).isFalse()
        expectThat(lockProvider.lock(lockConfiguration("other-job", lockAtMostFor = Duration.ofMinutes(5))).isPresent).isTrue()

        lock.get().unlock()

        expectThat(lockProvider.lock(lockConfiguration("job", lockAtMostFor = Duration.ofMinutes(5))).isPresent).isTrue()
    }

    @Test
    fun `should keep lease until the minimum lock time after unlocking`() {
        val lock =
            lockProvider.lock(
                lockConfiguration("job", lockAtMostFor = Duration.ofMinutes(5), lockAtLeastFor = Duration.ofMinutes(1)),
            )
        lock.get().unlock()

        expectThat(lockProvider.lock(lockConfiguration("job", lockAtMostFor = Duration.ofMinutes(5))).isPresent).isFalse()
    }

    @Test
    fun `should take over an expired lease`() {
        val expiredLock = lockProvider.lock(lockConfiguration("job", lockAtMostFor = Duration.ofMillis(200)))
        expectThat(expiredLock.isPresent).isTrue()

        Thread.sleep(300)

        val newLock = lockProvider.lock(lockConfiguration("job", lockAtMostFor = Duration.ofMinutes(5)))
        expectThat(newLock.isPresent).isTrue()

        // Releasing the expired lock must not release the lock that took over its lease
        expiredLock.get().unlock()
        expectThat(lockProvider.lock(lockConfiguration("job", lockAtMostFor = Duration.ofMinutes(5))).isPresent).isFalse()
    }

    @Test
    fun `only one of many concurrent attempts should get the lock`() {
        val attemptCount = 10
        val lockCount = AtomicInteger(0)
        val startLatch = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(attemptCount)

        repeat(attemptCount) {
            executor.submit {
                startLatch.await()
                if (lockProvider.lock(lockConfiguration("contended-job", lockAtMostFor = Duration.ofMinutes(5))).isPresent) {
                    lockCount.incrementAndGet()
                }
            }
        }
        startLatch.countDown()
        executor.shutdown()
        expectThat(executor.awaitTermination(1, TimeUnit.MINUTES)).isTrue()

        expectThat(lockCount.get()).isEqualTo(1)
    }

    @Test
    fun `should run scheduled job with object store lock`() {
        val jobRuns = CountDownLatch(1)
        val scheduler = Scheduler(SchedulerLock(lockProvider))

        scheduler.scheduleJob(ScheduledJob("object-store-scheduled-job", { jobRuns.countDown() }, ScheduleDelay(1, TimeUnit.SECONDS)))

        expectThat(jobRuns.await(30, TimeUnit.SECONDS)).isTrue()
    }

    private fun lockConfiguration(
        name: String,
        lockAtMostFor: Duration,
        lockAtLeastFor: Duration = Duration.ZERO,
    ) = LockConfiguration(Instant.now(), name, lockAtMostFor, lockAtLeastFor)

    companion object {
        private const val BUCKET_NAME = "persistenceparity"
    }
}
