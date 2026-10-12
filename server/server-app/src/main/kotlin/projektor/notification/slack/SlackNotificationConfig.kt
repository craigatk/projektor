package projektor.notification.slack

import io.ktor.server.config.ApplicationConfig
import projektor.server.api.config.SlackServerConfig
import java.math.BigDecimal

data class SlackNotificationConfig(
    val apiUrl: String?,
    val botToken: String?,
    val coverageDropThreshold: BigDecimal = DEFAULT_COVERAGE_DROP_THRESHOLD,
    val flakyMaxRuns: Int = DEFAULT_FLAKY_MAX_RUNS,
    val flakyFailureThreshold: Int = DEFAULT_FLAKY_FAILURE_THRESHOLD,
) {
    val enabled: Boolean
        get() = !apiUrl.isNullOrBlank() && !botToken.isNullOrBlank()

    fun toServerConfig(): SlackServerConfig = SlackServerConfig(enabled = enabled)

    companion object {
        val DEFAULT_COVERAGE_DROP_THRESHOLD = BigDecimal("0.1")

        // Same defaults as the repository flaky tests routes
        const val DEFAULT_FLAKY_MAX_RUNS = 50
        const val DEFAULT_FLAKY_FAILURE_THRESHOLD = 5

        fun createSlackNotificationConfig(applicationConfig: ApplicationConfig): SlackNotificationConfig {
            val apiUrl = applicationConfig.propertyOrNull("ktor.notification.slack.apiUrl")?.getString()
            val botToken = applicationConfig.propertyOrNull("ktor.notification.slack.botToken")?.getString()
            val coverageDropThreshold =
                applicationConfig.propertyOrNull("ktor.notification.slack.coverageDropThreshold")?.getString()?.toBigDecimal()
            val flakyMaxRuns = applicationConfig.propertyOrNull("ktor.notification.slack.flakyMaxRuns")?.getString()?.toInt()
            val flakyFailureThreshold =
                applicationConfig.propertyOrNull("ktor.notification.slack.flakyFailureThreshold")?.getString()?.toInt()

            return SlackNotificationConfig(
                apiUrl = apiUrl,
                botToken = botToken,
                coverageDropThreshold = coverageDropThreshold ?: DEFAULT_COVERAGE_DROP_THRESHOLD,
                flakyMaxRuns = flakyMaxRuns ?: DEFAULT_FLAKY_MAX_RUNS,
                flakyFailureThreshold = flakyFailureThreshold ?: DEFAULT_FLAKY_FAILURE_THRESHOLD,
            )
        }
    }
}
