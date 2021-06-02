package projektor.telemetry

import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.util.*
import io.opentelemetry.api.trace.Span

class OpenTelemetryRoute {

    class Configuration {
        var includeMethod: Boolean = false
    }

    companion object Feature : ApplicationFeature<Application, Configuration, OpenTelemetryRoute> {
        override val key: AttributeKey<OpenTelemetryRoute> = AttributeKey("OpenTelemetryRoute")

        override fun install(pipeline: Application, configure: Configuration.() -> Unit): OpenTelemetryRoute {
            val configuration = Configuration().apply(configure)

            pipeline.environment.monitor.subscribe(Routing.RoutingCallStarted) { call ->
                val routeWithMethod = call.route.toString()

                val routeAttribute = if (configuration.includeMethod) {
                    routeWithMethod
                } else {
                    val routeWithoutMethod = routeWithMethod.substring(0, routeWithMethod.indexOf("("))
                    routeWithoutMethod
                }

                Span.current()?.setAttribute("ktor.route", routeAttribute)
            }

            return OpenTelemetryRoute()
        }
    }
}
