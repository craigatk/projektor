package projektor.metrics

import io.micrometer.core.instrument.Clock
import io.micrometer.influx.InfluxConfig
import io.micrometer.influx.InfluxMeterRegistry
import java.time.Duration

fun createRegistry(config: InfluxMetricsConfig): InfluxMeterRegistry {
    val registryConfig: InfluxConfig =
        object : InfluxConfig {
            override fun step(): Duration {
                return Duration.ofSeconds(config.interval)
            }

            override fun db(): String {
                return config.dbName
            }

            override fun get(k: String): String? {
                val value =
                    when (k) {
                        "influx.autoCreateDb" -> config.autoCreateDb.toString()
                        "influx.enabled" -> config.enabled.toString()
                        "influx.uri" -> config.uri
                        "influx.userName" -> config.username
                        "influx.password" -> config.password
                        else -> null
                    }

                return value
            }
        }

    return InfluxMeterRegistry(registryConfig, Clock.SYSTEM)
}
