package projektor.testcase.cluster

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import projektor.ai.analysis.TestFailureAnalysis
import projektor.server.api.PublicId
import java.util.concurrent.ConcurrentHashMap

/**
 * Remembers AI analyses per (test run, failure signature) so failures that share a root cause
 * are only sent to the AI provider once, no matter which failing test is analyzed first.
 * Failed (null) analyses aren't cached so they can be retried.
 */
class FailureAnalysisCache(private val maxEntries: Int = 1_000) {
    private val analyses =
        object : LinkedHashMap<String, TestFailureAnalysis>(16, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, TestFailureAnalysis>?) = size > maxEntries
        }

    // One lock per key so concurrent requests for the same cluster share one AI call
    private val keyLocks = ConcurrentHashMap<String, Mutex>()

    suspend fun getOrAnalyze(
        publicId: PublicId,
        signatureKey: String,
        analyze: suspend () -> TestFailureAnalysis?,
    ): TestFailureAnalysis? {
        val cacheKey = "${publicId.id}:$signatureKey"

        get(cacheKey)?.let { return it }

        val lock = keyLocks.computeIfAbsent(cacheKey) { Mutex() }
        return try {
            lock.withLock {
                get(cacheKey) ?: analyze()?.also { put(cacheKey, it) }
            }
        } finally {
            if (!lock.isLocked) keyLocks.remove(cacheKey, lock)
        }
    }

    private fun get(cacheKey: String): TestFailureAnalysis? = synchronized(analyses) { analyses[cacheKey] }

    private fun put(
        cacheKey: String,
        analysis: TestFailureAnalysis,
    ) = synchronized(analyses) { analyses[cacheKey] = analysis }
}
