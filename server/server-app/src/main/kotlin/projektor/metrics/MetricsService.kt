package projektor.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer

class MetricsService(private val metricRegistry: MeterRegistry) {
    private val resultsProcessSuccessCounter = metricRegistry.counter("results_process_success")
    private val resultsProcessFailureCounter = metricRegistry.counter("results_process_failure")

    fun incrementResultsProcessSuccessCounter() = resultsProcessSuccessCounter.increment()

    fun incrementResultsProcessFailureCounter() = resultsProcessFailureCounter.increment()

    fun createTimer(name: String): Pair<Timer, Timer.Sample> {
        val timer = metricRegistry.timer(name)
        val sample = Timer.start(metricRegistry)
        return Pair(timer, sample)
    }

    fun stopTimer(timerPair: Pair<Timer, Timer.Sample>) {
        val timer = timerPair.first
        val sample = timerPair.second
        sample.stop(timer)
    }
}
