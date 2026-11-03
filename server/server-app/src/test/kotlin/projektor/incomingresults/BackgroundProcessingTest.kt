package projektor.incomingresults

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.Test
import org.koin.ktor.ext.get
import projektor.ApplicationTestCase
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class BackgroundProcessingTest : ApplicationTestCase() {
    private val backgroundProcessing = BackgroundProcessing()

    @Test
    fun `should keep running jobs after a job throws an exception`() {
        backgroundProcessing.launch("failing") { throw IllegalStateException("Simulated failure") }
        expectThat(backgroundProcessing.awaitCompletion(10.seconds)).isTrue()

        val laterJobRan = AtomicBoolean()
        backgroundProcessing.launch("later") { laterJobRan.set(true) }

        await until { laterJobRan.get() }
    }

    @Test
    fun `should keep running jobs after a job throws an error`() {
        backgroundProcessing.launch("failing") { throw OutOfMemoryError("Simulated failure") }
        expectThat(backgroundProcessing.awaitCompletion(10.seconds)).isTrue()

        val laterJobRan = AtomicBoolean()
        backgroundProcessing.launch("later") { laterJobRan.set(true) }

        await until { laterJobRan.get() }
    }

    @Test
    fun `waiting for completion should wait for running jobs`() {
        val finished = AtomicBoolean()

        backgroundProcessing.launch("slow") {
            delay(200.milliseconds)
            finished.set(true)
        }

        expectThat(backgroundProcessing.awaitCompletion(10.seconds)).isTrue()
        expectThat(finished.get()).isTrue()
    }

    @Test
    fun `waiting for completion should give up after the timeout`() {
        val neverFinishes = CompletableDeferred<Unit>()

        backgroundProcessing.launch("stuck") { neverFinishes.await() }

        expectThat(backgroundProcessing.awaitCompletion(50.milliseconds)).isFalse()

        neverFinishes.complete(Unit)
    }

    @Test
    fun `stopping the application should wait for uploads still processing`() {
        val finished = AtomicBoolean()

        projektorTestApplication {
            val applicationBackgroundProcessing: BackgroundProcessing = getApplication().get()

            applicationBackgroundProcessing.launch("slow") {
                delay(500.milliseconds)
                finished.set(true)
            }
        }

        expectThat(finished.get()).isEqualTo(true)
    }
}
