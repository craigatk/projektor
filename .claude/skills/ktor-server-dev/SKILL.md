---
name: ktor-server-dev
description: Use when adding/modifying HTTP routes, auth checks, config values, or cross-cutting Ktor plugins in server/server-app — the general application/HTTP layer (as opposed to the MCP-specific server, covered by mcp-server-dev, or the jOOQ/Postgres layer, covered by database-persistence). Covers the Route-extension-function file convention, the manual header-token auth pattern, the lack of a StatusPages plugin (error handling is per-route try/catch), config loading via ApplicationConfig + per-subsystem *Config.createXConfig factories, and how to write an HTTP-level test with ApplicationTestCase.
user-invocable: true
---

# Working with Projektor's Ktor server (HTTP layer)

Entry point: `server/server-app/src/main/kotlin/projektor/Application.kt`, `fun
Application.main(...)`. It builds config objects, installs plugins, then registers ~20 route
files inside one `routing { ... }` block, followed by the MCP server (see the
`mcp-server-dev` skill for that). Repository/DI wiring is covered by the
`database-persistence` skill — this skill is about routes, auth, config, and plugins.

## Route file convention

One `fun Route.xxx(...)` extension per file under
`server/server-app/src/main/kotlin/projektor/route/`, `package projektor.route`. Services/
config are passed in as plain params (already resolved via Koin `by inject()` in
`Application.kt`) — no DI inside the route file itself:

```kotlin
// simplest shape (HealthRoute.kt)
fun Route.health() {
    get("/health") {
        call.respond(HttpStatusCode.OK, Status("OK"))
    }
}

// path param + service call (TestRunRoutes.kt)
fun Route.testRuns(testRunService: TestRunService) {
    get("/run/{publicId}") {
        val publicId = call.parameters.getOrFail("publicId")
        testRunService.fetchTestRun(PublicId(publicId))
            ?.let { call.respond(HttpStatusCode.OK, it) }
            ?: call.respond(HttpStatusCode.NotFound)
    }
}
```

Conventions to match:
- Path params: `call.parameters.getOrFail("name")` (throws Ktor's
  `MissingRequestParameterException` if absent — nothing catches this locally, see "Error
  handling" below, so a missing required param currently surfaces as an uncaught 500, not a
  clean 400).
- Query params: no framework helper, just `call.request.queryParameters["x"]` with a manual
  `?.toInt()`/`?.let`/`?: default` — see `ApiRoutes.kt`, `TestSuiteRoutes.kt`.
- Not-found convention: `result?.let { call.respond(OK, it) } ?: call.respond(NotFound)`.
  Some coverage routes use `NoContent` instead of `NotFound` when the parent resource exists
  but the specific sub-resource doesn't yet — match whichever distinction the existing
  sibling routes in that file make, don't default to `NotFound` everywhere.
- Register the new route function inside `Application.kt`'s single `routing { }` block,
  alphabetically among the existing calls (that block is already alphabetized).
- Externally-consumed org/repo-level endpoints live under an explicit `/api/v1/...` prefix
  (`ApiRoutes.kt` — flaky tests, coverage, run summaries). Most other routes are unprefixed
  (`/run/{publicId}/...`, `/results`, `/health`). Follow whichever convention matches the
  audience of the new route — internal/UI-consumed vs. external API.
- Request bodies are NOT auto-decompressed by Ktor's `Compression` plugin (that's
  response-only here). If a route needs to accept a possibly-gzipped body (as the results/
  coverage upload routes do), use the existing helper:
  `projektor.route.CompressionRequest.receiveCompressedOrPlainTextPayload(call)` rather than
  `call.receiveText()` directly.

## Auth: manual header check, not a Ktor Authentication plugin

There's no `Authentication` plugin installed. `AuthService`
(`server/server-app/src/main/kotlin/projektor/auth/AuthService.kt`) is a single shared
"publish token" gate:
```kotlin
class AuthService(private val authConfig: AuthConfig) {
    fun isAuthValid(tokenFromRequest: String?) =
        authConfig.publishToken.isNullOrEmpty() || authConfig.publishToken == tokenFromRequest
}
```
If no token is configured (`ktor.auth.publishToken` unset), auth is a no-op — everything
passes. The header name is `AuthConfig.PUBLISH_TOKEN` (`"X-PROJEKTOR-TOKEN"`). Call sites
check it inline, only on mutating/publish endpoints, never on GETs:
```kotlin
if (!authService.isAuthValid(call.request.header(AuthConfig.PUBLISH_TOKEN))) {
    call.respond(HttpStatusCode.Unauthorized)
} else {
    // ... do the work
}
```
A new route needing this same gate takes `authService: AuthService` as a param (see
`attachments(attachmentService, authService)`, `coverage(authService, coverageService)` in
`Application.kt`) and repeats this check — there's no route-level annotation/plugin shortcut
for it.

## Error handling: no StatusPages, per-route try/catch

There is **no `StatusPages` plugin installed** anywhere in `server-app` — this is a real gap,
not an oversight to route around. Consequences:
- Expected failure cases are explicit: `call.respond(HttpStatusCode.X, someErrorBody)`.
- A service call that can throw a domain exception needs its own local
  `try { ... } catch (e: Exception) { call.respond(HttpStatusCode.BadRequest, ErrorBody(...)) }`
  — see `CoverageRoutes.kt` and `ResultsRoutes.kt` (which catches
  `PersistTestResultsException`, a `RuntimeException` subtype carrying `publicId` +
  `errorMessage`, and turns it into a `SaveResultsError` body) for the pattern to copy.
- Anything **not** explicitly caught (including `MissingRequestParameterException` from a
  missing `getOrFail` path param) falls through to Ktor's default engine-level handling — a
  bare 500 with no normalized error body. When adding a route, decide deliberately whether a
  given failure mode should be a clean 4xx (add the try/catch or validate up front) or is
  acceptable as an unhandled 500 — don't assume a global handler will clean it up later.

## Config: ApplicationConfig + per-subsystem factories

`server/server-app/src/main/resources/application.conf` (HOCON), one nested block per
subsystem (`ktor.datasource`, `ktor.auth`, `ktor.cleanup`, `ktor.attachment`,
`ktor.metrics.influxdb`, ...). Every value is env-var overridable via `${?ENV_VAR}`:
```
auth {
    publishToken = ${?PUBLISH_TOKEN}
}
```
so in real deployments config effectively comes from env vars; `application.conf` mostly just
declares which env var maps to which key (some, like `auth.publishToken`, have no local
default at all).

Convention for a config area: a `data class XConfig(...)` with a companion
`fun createXConfig(applicationConfig: ApplicationConfig): XConfig` that reads values via
`applicationConfig.propertyOrNull("ktor.x.y")?.getString()` — **never** a bare `.getString()`
that throws on a missing key. Example (`CleanupConfig.kt`):
```kotlin
data class CleanupConfig(val maxReportAgeDays: Int?, val dryRun: Boolean) {
    companion object {
        fun createCleanupConfig(applicationConfig: ApplicationConfig): CleanupConfig {
            val maxReportAgeDays = applicationConfig.propertyOrNull("ktor.cleanup.maxReportAgeDays")
                ?.getString()?.toInt()
            ...
            return CleanupConfig(maxReportAgeDays, dryRun)
        }
    }
}
```
All `create*Config(applicationConfig)` calls happen up front in `Application.kt::main`, and
the resulting objects are passed into route functions and/or `AppModule.kt`'s
`createAppModule(...)`.

**To add a new config value**: add the key + `${?ENV_VAR}` line to `application.conf` → add/
extend the relevant `data class XConfig` + `createXConfig` factory in that subsystem's
package → call the factory in `Application.kt` → if a route or the `/config` endpoint
(`ConfigRoutes.kt`, exposes select non-secret config to the frontend as `ServerConfig`)
needs it, wire it through → if tests need to control it, add a field to
`ApplicationTestCaseConfig` (see below) so it can be overridden per-test.

## Cross-cutting plugins (installed in `Application.kt::main`, in order)

- **CORS**: `install(CORS) { anyHost() }` — wide open, intentional (logback even silences its
  warnings). No per-route CORS scoping exists.
- **ContentNegotiation**: Jackson only, `SNAKE_CASE`, `JavaTimeModule`, dates not written as
  timestamps, unknown properties ignored on deserialize. Also carries the MCP-specific
  `registerMcpTypeCompatibility()` mixin registration — see the `mcp-server-dev` skill, not
  relevant to ordinary REST routes.
- **Koin**: DI wiring centralized in `AppModule.kt::createAppModule(...)` — see the
  `database-persistence` skill for the repository-binding half of this.
- **CachingHeaders**: only sets a 7-day max-age on `application/javascript` responses;
  everything else gets no explicit caching header.
- **MicrometerMetrics**: conditionally installed (`if (influxMetricsConfig.enabled)`), tags
  timers with `env` if configured. App-specific counters beyond Micrometer's auto-instrumentation
  go through a separate hand-rolled `MetricsService` called directly from route handlers
  (e.g. `metricsService.incrementResultsProcessStartCounter()` in `ResultsRoutes.kt`) — add a
  new counter there, not as a custom Micrometer tag.
- **Compression**: response-only, gzip, scoped to `application/json` and
  `application/javascript`. See the request-decompression note under "Route file convention".
- **IgnoreTrailingSlash**: installed so `/tests/` and `/tests` are treated the same.
- **No `CallLogging`** — there's no automatic access log; logging is ad-hoc `log.info(...)`
  per-service. Don't assume incoming requests are logged anywhere by default.
- **No WebSocket/SSE** in `server-app` proper (the MCP server's own streamable-HTTP transport
  is a separate concern).
- **Graceful shutdown**: `environment.monitor.subscribe(ApplicationStopped) { dataSource.close() }`
  at the bottom of `main` — if you add another closeable resource at app scope, close it here too.

## Testing HTTP endpoints

Base class: `server/server-app/src/test/kotlin/projektor/ApplicationTestCase.kt`, exposing
`projektorTestApplication(testCaseConfig = ApplicationTestCaseConfig(), block)`:
```kotlin
@Test
fun `my test`() = projektorTestApplication {
    val response = client.get("/health")
    expectThat(response.status).isEqualTo(HttpStatusCode.OK)
}
```
- `client` is Ktor's test `HttpClient`, available for free inside the block.
- Assertions use **Strikt** (`expectThat(...).isEqualTo(...)`, `.contains(...)`, `.not { }`),
  not raw JUnit/kotlin.test assertions — match existing test style.
- `ApplicationTestCaseConfig` (data class of overrides: `publishToken`,
  `attachmentsEnabled`, `metricsEnabled`, cleanup ages, etc.) maps into a
  `MapApplicationConfig` under the same `ktor.*` keys `application.conf` uses — this is how a
  single test turns auth/attachments/metrics on or off without touching real config files.
  See `AddAttachmentTokenApplicationTest.kt` for the auth-testing pattern:
  `ApplicationTestCaseConfig(publishToken = "publish12345", ...)` +
  `headers { append(AuthConfig.PUBLISH_TOKEN, "publish12345") }`.
- Response bodies are deserialized with the base class's `objectMapper` (Jackson, configured
  identically to the app's — SNAKE_CASE, ignore-unknown-props):
  `objectMapper.readValue(response.bodyAsText(), X::class.java)`.
- A handful of test-only `HttpClient` extension helpers live directly in
  `ApplicationTestCase.kt` for common multi-step flows — `postGroupedResultsJSON(...)`,
  `postResultsPlainText(...)`, `waitForTestRunSaveToComplete(response)` (polls with
  Awaitility until async processing finishes), `waitUntilTestRunHasAttachments(publicId,
  count)`. Prefer these over hand-rolling the same POST-then-poll sequence again.
- Tests are organized by feature, mirroring routes: `server/server-app/src/test/kotlin/
  projektor/<feature>/...ApplicationTest.kt` (e.g. `attachment/`, `testsuite/`, `health/`),
  not one giant test class — put a new test in the matching feature package.

## Local dev / testing commands

| Goal | Command |
|---|---|
| Compile | `./gradlew :server:server-app:compileKotlin` |
| Run one feature's HTTP tests | `./gradlew :server:server-app:test --tests "projektor.testrun.*"` |
| Run the full server-app suite | `./gradlew :server:server-app:test` |
| Lint | `./gradlew :server:server-app:ktlintCheck` |
