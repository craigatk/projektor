package projektor.telemetry

import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.util.*
import io.opentelemetry.api.trace.Span

class OpenTelemetryRoute {

    class Configuration {
        var includeHttpMethod: Boolean = false
    }

    companion object Feature : ApplicationFeature<Application, Configuration, OpenTelemetryRoute> {
        override val key: AttributeKey<OpenTelemetryRoute> = AttributeKey("OpenTelemetryRoute")

        override fun install(pipeline: Application, configure: Configuration.() -> Unit): OpenTelemetryRoute {
            val configuration = Configuration().apply(configure)

            pipeline.environment.monitor.subscribe(Routing.RoutingCallStarted) { call ->
                val routeWithMethod = call.route.toString()

                // By default, Ktor's route value includes the HTTP method such as "/run/{publicId}/(method: 'GET')"
                // Conditionally remove the method part of the route so the attribute is "/run/{publicId}/" instead and easier to query by
                val routeAttribute = if (configuration.includeHttpMethod) routeWithMethod else extractRouteWithoutMethod(routeWithMethod)

                val currentSpan = Span.current()
                currentSpan?.setAttribute("ktor.route", routeAttribute)
            }

            return OpenTelemetryRoute()
        }

        private fun extractRouteWithoutMethod(routeWithMethod: String) = routeWithMethod.substring(0, routeWithMethod.indexOf("("))
    }
}
