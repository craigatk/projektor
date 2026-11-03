package projektor.persistence.objectstore

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.Logger

// Keeps one request or job that fans out over many test runs from using every Dispatchers.IO thread
const val MAX_CONCURRENT_RUN_READS = 16

/**
 * Applies the blocking transform to every item on the IO dispatcher, with at most maxConcurrency running at a time.
 * Keeps the given order.
 */
suspend fun <T, R> List<T>.mapParallel(
    maxConcurrency: Int = MAX_CONCURRENT_RUN_READS,
    transform: suspend (T) -> R,
): List<R> =
    coroutineScope {
        val semaphore = Semaphore(maxConcurrency)

        map { item -> async(Dispatchers.IO) { semaphore.withPermit { transform(item) } } }.awaitAll()
    }

/**
 * Like [mapParallel] for a scan that runs again later: an item that fails is logged and left out,
 * so one unreadable document doesn't stop the scan from finding everything else. Null results are left out too.
 */
suspend fun <T, R : Any> List<T>.mapParallelSkippingFailures(
    logger: Logger,
    transform: suspend (T) -> R?,
): List<R> =
    mapParallel { item ->
        try {
            transform(item)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warn("Skipping $item in this scan since reading it failed", e)
            null
        }
    }.filterNotNull()
