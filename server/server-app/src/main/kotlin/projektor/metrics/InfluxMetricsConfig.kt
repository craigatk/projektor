package projektor.metrics

import io.ktor.server.config.ApplicationConfig

data class InfluxMetricsConfig(
    val enabled: Boolean,
    val dbName: String,
    val uri: String,
    val username: String?,
    val password: String?,
    val autoCreateDb: Boolean,
    val interval: Long,
    val environment: String?
) {
    companion object {
        fun createInfluxMetricsConfig(applicationConfig: ApplicationConfig) = InfluxMetricsConfig(
            applicationConfig.propertyOrNull("ktor.metrics.influxdb.enabled")?.getString()?.toBoolean() ?: false,
            applicationConfig.propertyOrNull("ktor.metrics.influxdb.dbName")?.getString() ?: "default",
            applicationConfig.propertyOrNull("ktor.metrics.influxdb.uri")?.getString() ?: "http://localhost:8086",
            applicationConfig.propertyOrNull("ktor.metrics.influxdb.username")?.getString(),
            applicationConfig.propertyOrNull("ktor.metrics.influxdb.password")?.getString(),
            applicationConfig.propertyOrNull("ktor.metrics.influxdb.autoCreateDb")?.getString()?.toBoolean() ?: false,
            applicationConfig.propertyOrNull("ktor.metrics.influxdb.interval")?.getString()?.toLong() ?: 10,
            applicationConfig.propertyOrNull("ktor.metrics.influxdb.environment")?.getString()
        )
    }
}
