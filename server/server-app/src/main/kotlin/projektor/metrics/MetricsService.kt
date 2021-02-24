package projektor.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer

class MetricsService(private val metricRegistry: MeterRegistry) {
    private val coverageParseFailureCounter = metricRegistry.counter("coverage_parse_failure")
    private val coverageProcessFailureCounter = metricRegistry.counter("coverage_process_failure")
    private val coverageProcessSuccessCounter = metricRegistry.counter("coverage_process_success")
    private val coverageProcessStartCounter = metricRegistry.counter("coverage_process_start")

    private val pullRequestCommentSuccessCounter = metricRegistry.counter("pull_request_comment_success")
    private val pullRequestCommentFailureCounter = metricRegistry.counter("pull_request_comment_failure")

    private val resultsProcessStartCounter = metricRegistry.counter("results_process_start")
    private val resultsProcessSuccessCounter = metricRegistry.counter("results_process_success")
    private val resultsProcessFailureCounter = metricRegistry.counter("results_process_failure")
    private val resultsParseFailureCounter = metricRegistry.counter("results_parse_failure")

    fun incrementCoverageParseFailureCounter() = coverageParseFailureCounter.increment()
    fun incrementCoverageProcessFailureCounter() = coverageProcessFailureCounter.increment()
    fun incrementCoverageProcessSuccessCounter() = coverageProcessSuccessCounter.increment()
    fun incrementCoverageProcessStartCounter() = coverageProcessStartCounter.increment()

    fun incrementPullRequestCommentSuccessCounter() = pullRequestCommentSuccessCounter.increment()
    fun incrementPullRequestCommentFailureCounter() = pullRequestCommentFailureCounter.increment()

    fun incrementResultsProcessStartCounter() = resultsProcessStartCounter.increment()
    fun incrementResultsProcessSuccessCounter() = resultsProcessSuccessCounter.increment()
    fun incrementResultsProcessFailureCounter() = resultsProcessFailureCounter.increment()
    fun incrementResultsParseFailureCounter() = resultsParseFailureCounter.increment()

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
