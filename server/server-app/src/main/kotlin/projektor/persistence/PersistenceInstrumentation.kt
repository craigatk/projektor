package projektor.persistence

import io.micrometer.core.instrument.MeterRegistry
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.withContext
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.slf4j.LoggerFactory
import projektor.server.api.PublicId
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.coroutines.Continuation
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.intercepted
import kotlin.coroutines.intrinsics.startCoroutineUninterceptedOrReturn
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resumeWithException

/**
 * Wraps repositories so every call, on either persistence backend, records:
 *
 * - a `persistence_repository_call` timer tagged with `backend`, `repository`, `method` and `outcome`
 *   (`success`, `error` or `cancelled`)
 * - a span named `projektor.<Repository>.<method>`, which is the current span while the call runs so
 *   spans for the database queries or object store requests it makes are nested under it
 * - a warning log when the call takes longer than the slow call threshold
 *
 * Done with a dynamic proxy on the repository interface so the 17 repository interfaces don't each need a
 * hand-written decorator. Suspend functions are timed until they complete, not until they first suspend.
 */
class PersistenceInstrumentation(
    private val backend: PersistenceType,
    private val meterRegistry: MeterRegistry,
    private val slowCallThreshold: Duration = PersistenceConfig.DEFAULT_SLOW_CALL_THRESHOLD,
    private val tracer: Tracer = GlobalOpenTelemetry.getTracer("projektor.persistence"),
) {
    inline fun <reified T : Any> instrument(repository: T): T = instrument(T::class.java, repository)

    fun <T : Any> instrument(
        repositoryInterface: Class<T>,
        repository: T,
    ): T {
        require(repositoryInterface.isInterface) { "${repositoryInterface.name} must be an interface to instrument it" }

        val proxy =
            Proxy.newProxyInstance(
                repositoryInterface.classLoader,
                arrayOf(repositoryInterface),
                RepositoryInvocationHandler(repositoryInterface.simpleName, repository),
            )

        return repositoryInterface.cast(proxy)
    }

    private inner class RepositoryInvocationHandler(
        private val repositoryName: String,
        private val repository: Any,
    ) : InvocationHandler {
        override fun invoke(
            proxy: Any,
            method: Method,
            args: Array<out Any?>?,
        ): Any? {
            val arguments = args ?: arrayOf()

            // equals, hashCode and toString
            if (method.declaringClass == Any::class.java) {
                return invokeRepository(method, arguments)
            }

            @Suppress("UNCHECKED_CAST")
            val continuation = arguments.lastOrNull() as? Continuation<Any?>

            return if (continuation != null) {
                invokeSuspending(method, arguments, continuation)
            } else {
                val call = RepositoryCall(repositoryName, method.name, arguments)
                try {
                    call.span.makeCurrent().use { invokeRepository(method, arguments) }
                        .also { call.finish(null) }
                } catch (e: Throwable) {
                    call.finish(e)
                    throw e
                }
            }
        }

        private fun invokeSuspending(
            method: Method,
            arguments: Array<out Any?>,
            continuation: Continuation<Any?>,
        ): Any? {
            val call = RepositoryCall(repositoryName, method.name, arguments.copyOfRange(0, arguments.size - 1))

            val instrumentedCall: suspend () -> Any? = {
                try {
                    withContext<Any?>(call.span.asContextElement()) {
                        suspendCoroutineUninterceptedOrReturn<Any?> { repositoryContinuation ->
                            val repositoryArguments: Array<Any?> = arrayOf(*arguments)
                            repositoryArguments[repositoryArguments.lastIndex] = repositoryContinuation
                            invokeRepository(method, repositoryArguments)
                        }
                    }.also { call.finish(null) }
                } catch (e: Throwable) {
                    call.finish(e)
                    throw e
                }
            }

            return try {
                instrumentedCall.startCoroutineUninterceptedOrReturn(continuation)
            } catch (e: Throwable) {
                // Hand the exception to the caller through its continuation, since throwing a checked exception
                // from here would get it wrapped in an UndeclaredThrowableException by the proxy
                continuation.intercepted().resumeWithException(e)
                COROUTINE_SUSPENDED
            }
        }

        private fun invokeRepository(
            method: Method,
            arguments: Array<out Any?>,
        ): Any? =
            try {
                method.invoke(repository, *arguments)
            } catch (e: InvocationTargetException) {
                throw e.targetException
            }
    }

    private inner class RepositoryCall(
        private val repositoryName: String,
        private val methodName: String,
        arguments: Array<out Any?>,
    ) {
        private val startNanos = System.nanoTime()

        val span: Span =
            tracer.spanBuilder("projektor.$repositoryName.$methodName")
                .setAttribute(BACKEND_ATTRIBUTE, backend.configValue)
                .startSpan()

        init {
            arguments.filterIsInstance<PublicId>().firstOrNull()?.let { span.setAttribute(PUBLIC_ID_ATTRIBUTE, it.id) }
        }

        fun finish(error: Throwable?) {
            val durationNanos = System.nanoTime() - startNanos

            val outcome =
                when (error) {
                    null -> SUCCESS
                    is CancellationException -> CANCELLED
                    else -> ERROR
                }

            if (error != null && error !is CancellationException) {
                span.recordException(error)
                span.setStatus(StatusCode.ERROR)
            }
            span.end()

            meterRegistry.timer(
                CALL_TIMER,
                "backend",
                backend.configValue,
                "repository",
                repositoryName,
                "method",
                methodName,
                "outcome",
                outcome,
            ).record(durationNanos, TimeUnit.NANOSECONDS)

            val durationMillis = TimeUnit.NANOSECONDS.toMillis(durationNanos)
            if (durationMillis >= slowCallThreshold.toMillis()) {
                logger.warn(
                    "Slow {} persistence call {}.{} took {} ms (outcome {})",
                    backend.configValue,
                    repositoryName,
                    methodName,
                    durationMillis,
                    outcome,
                )
            }
        }
    }

    companion object {
        const val CALL_TIMER = "persistence_repository_call"

        const val SUCCESS = "success"
        const val ERROR = "error"
        const val CANCELLED = "cancelled"

        private const val BACKEND_ATTRIBUTE = "projektor.persistence.backend"
        private const val PUBLIC_ID_ATTRIBUTE = "projektor.id"

        private val logger = LoggerFactory.getLogger(PersistenceInstrumentation::class.java)
    }
}

/**
 * Defines a repository singleton that's wrapped by [PersistenceInstrumentation], bound to its interface [T].
 */
inline fun <reified T : Any> Module.instrumentedSingle(
    instrumentation: PersistenceInstrumentation,
    crossinline create: Scope.() -> T,
) = single<T> { instrumentation.instrument<T>(create()) }
