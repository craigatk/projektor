package projektor.incomingresults

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.slf4j.LoggerFactory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Runs the processing of uploads that have already been accepted, after their requests have finished.
 *
 * Jobs are independent: with a plain scope, one job failing would cancel the scope, and every later upload would be
 * accepted but never processed.
 */
class BackgroundProcessing {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * @param description what the job is doing, for logging if it fails. Jobs are expected to record their own
     * processing errors, but recording them can fail too (e.g. when the store is unavailable).
     */
    fun launch(
        description: String,
        block: suspend () -> Unit,
    ) {
        scope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                logger.error("Failed $description", e)
            }
        }
    }

    /**
     * Waits for the running jobs to finish, for shutting down without losing accepted uploads.
     * Returns whether they all finished in time.
     */
    fun awaitCompletion(timeout: Duration): Boolean =
        runBlocking {
            withTimeoutOrNull(timeout) {
                while (true) {
                    val jobs = scope.coroutineContext.job.children.toList()

                    if (jobs.isEmpty()) break

                    jobs.joinAll()
                    delay(10.milliseconds)
                }
                true
            } ?: false
        }
}
