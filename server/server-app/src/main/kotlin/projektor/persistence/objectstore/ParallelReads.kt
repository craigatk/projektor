package projektor.persistence.objectstore

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

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
