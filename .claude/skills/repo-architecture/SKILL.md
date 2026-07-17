---
name: repo-architecture
description: Use when you need the big-picture map of this repo — what Projektor is, what each Gradle/npm module does, how a test run flows from CI publish to UI render, the server-api/server-client/server-app split, and how local dev and production deployment work. This is the "start here" orientation skill; it deliberately doesn't duplicate the deep internals already covered by mcp-server-dev, database-persistence, or ktor-server-dev — it points to those instead. Use this before a broad refactor, when you're lost about where something lives, or when onboarding to an unfamiliar part of the codebase.
user-invocable: true
---

# Projektor: repo architecture

Projektor is a **test results and code coverage reporting server**: CI jobs (or local runs)
publish JUnit-style test results, coverage reports (Jacoco/Kover/Clover/Cobertura/Go), and
performance test results to it; it parses, persists, and renders them as a web dashboard, and
can post GitHub PR comments with links and coverage deltas. Live instance:
`live.projektor.dev`. A stale-but-useful high-level diagram exists at
`Projektor-architecture.drawio`.

Deep internals of three subsystems already have their own skills — **don't re-derive them
here, load the specific skill instead**:
- `mcp-server-dev` — the MCP server (`server/server-app/.../projektor/mcp/`, `/mcp` route).
- `database-persistence` — Flyway migrations, jOOQ codegen, repository pattern.
- `ktor-server-dev` — general Ktor routing/auth/config conventions in `server-app`.

## Module map (`settings.gradle` + `ui/`)

**Server core**
| Module | What it is |
|---|---|
| `server:server-app` | The deployed Ktor backend — routing, persistence wiring, packages the built UI into its jar. |
| `server:server-api` | Pure Kotlin DTOs/interfaces shared by server and clients (`TestRun`, `Coverage`, ...), no framework deps. |
| `server:server-client` | Retrofit-based Kotlin HTTP client library for JVM consumers of the deployed API. |

Dependency direction: `server-app` → `server-api` ← `server-client`. The app never depends on
its own client library — if you're changing a DTO shape, `server-api` is the one module both
sides actually share.

**Parsing** (`server:parsing:*`) — one module per input format, feeding into aggregators:
- Test result formats: `junit-results-parser`, `jest-xml-parser` → merged/post-processed by
  `test-results-parser`; `grouped-results-parser` handles multi-project payloads.
- Coverage formats: `jacoco-xml-parser`, `cobertura-xml-parser`, `go-coverage-parser` →
  aggregated by `coverage-parser`.
- `performance-results-parser` — performance test results.

**Persistence** (`server:persistence:*`)
- `database-schema` — Flyway + jOOQ. See the `database-persistence` skill.
- `object-store` — S3/Minio-compatible client for attachments (screenshots, logs).

**AI** (`server:ai:*`)
- `analysis` — `AITestFailureAnalyzer`, provider-agnostic failure-analysis interface.
- `openai` — `OpenAIAnalysisClient`, the ChatGPT-backed implementation plugged into `analysis`.

**Notification** (`server:notification:*`)
- `badge` — SVG status badges (test pass/fail %, coverage %) from templates.
- `github` — GitHub App JWT auth + PR comment posting.

**Test support** (`server:test:*`, not shipped)
- `test-fixtures`, `coverage-fixtures`, `performance-fixtures` — shared sample data/builders.
- `server-example-loader` — CLI that publishes example results to a running server
  (`../../gradlew run`, `SERVER_URL` env var) — the way to seed local dev data.
- `load-test` — load-testing utilities.

**Publishers** (client-side tools *other* projects' CI uses to push data in — not part of the
deployed server):
- `publishers:gradle-plugin` — Gradle plugin that gathers/publishes test results from a
  Gradle build.
- `publishers:node-script:publish-functional-test` — this repo's own use of the top-level
  `publishers/node-script` npm package (`projektor-publish`) to publish its own functional
  test results (dogfooding).

**Frontend**
- `ui` — React 19 + TypeScript, bundled by Vite, MUI component kit, `@reach/router`,
  `recharts` for charts, `axios` (+ `axios-cache-interceptor`, `axios-case-converter`) for
  HTTP. State is component-local/query-param-driven (`use-query-params`) plus axios response
  caching — there's no Redux/global store despite it appearing in some vendored
  `node_modules` skill listings (that's `@reduxjs/toolkit`'s own bundled docs, not something
  this app uses). Included as a Gradle module (Node Gradle plugin) so `server-app` can depend
  on its build output.

**Top-level test module**
- `functional-test` — full-stack Cypress e2e suite that boots the actual `server-app` jar and
  runs Cypress against the real, fully-wired system (distinct from `ui/cypress`, which runs
  against just the Vite dev server with mocked/lighter setup — see the `cypress-debug` skill
  for `ui/cypress` specifically).
- `cypress-common` (repo root) — shared Cypress support code (`support/commands.js`) reused
  by *both* `ui/cypress` and `functional-test/cypress`. It's a shared library, not a
  standalone suite.

## End-to-end workflow: CI publish → UI render

1. **Publish** — a CI job uses `publishers:gradle-plugin` (Gradle projects) or the npm
   `projektor-publish` script (`publishers/node-script`, JS/Jest/Cypress projects) to gather
   local JUnit/coverage XML and POST it to the server. These publisher tools run *outside*
   this deployment, in whatever repo is being tested.
2. **Receive** — `server-app`'s `route/ResultsRoutes.kt` (`POST /results`, `POST
   /groupedResults`) hands off to `projektor/incomingresults/` services
   (`TestResultsService`, `TestResultsProcessingService`, `GroupedTestResultsService`,
   `AppendTestResultsService`).
3. **Parse** — payload goes to the matching `server:parsing:*` format module via the
   aggregators (`test-results-parser` / `coverage-parser` / `grouped-results-parser`),
   producing a common internal model.
4. **Persist** — `incomingresults/mapper/` (`ParsedResultsToDBMapper`, `ApiToDBMapper`) maps
   the parsed model into jOOQ writes (see `database-persistence` skill); large attachments go
   to `server:persistence:object-store`.
5. **Post-processing** — AI failure analysis (`server:ai:*`) can run against failures;
   badges (`server:notification:badge`) generate on demand; GitHub PR comments
   (`server:notification:github`) post if configured.
6. **Serve to UI** — the rest of `server-app`'s `route/*Routes.kt` files expose read APIs;
   `ui/` fetches over REST via axios and renders the dashboard. The MCP server
   (`mcp-server-dev` skill) exposes a subset of the same data through a second, LLM-facing
   interface rather than a new pipeline.

## Local dev loop

1. `docker-compose up` — Postgres (port 5433→5432) + Minio (port 9000).
2. Backend: `./gradlew :server:server-app:run` → `http://localhost:8080`.
3. Frontend: `cd ui && yarn install && yarn start` → `http://localhost:1234` (Vite dev
   server, `vite serve src --port 1234`, auto-points at `localhost:8080`).
4. Seed data: `server:test:server-example-loader`'s `../../gradlew run` publishes example
   results to the local server.
5. `yarn build`/`yarn build:dev` in `ui/` produce the static bundle `server-app` packages;
   `./gradlew :server:server-app:assembleFull` does the full build-UI-then-fat-jar pipeline
   (`server-app-1.0-all.jar`, via the Shadow plugin) — this is what CI/Docker actually run,
   not something you need for day-to-day backend dev.

## Deployment

Two deploy paths coexist in the repo:
- **Docker / DigitalOcean App Platform** (current, live.projektor.dev): root `Dockerfile`
  installs Node+Yarn, runs `./gradlew :server:server-app:assembleFull --no-daemon
  --no-parallel` (the `--no-parallel` is deliberate — DO's build container has less memory
  than CI, and `gradle.properties`' `org.gradle.jvmargs=-Xmx2g` exists for the same reason),
  then `docker-entrypoint.sh` runs the resulting jar. No committed App Platform spec/`doctl`
  config — presumably configured directly in DO's dashboard against this Dockerfile.
- **Heroku** (older, still wired up): root `Procfile` + a `heroku {}` block and
  `deployHeroku` task in `server/server-app/build.gradle` (`appName = "projektorlive"`).
  `ui/README.md` still references the old `projektorlive.herokuapp.com` URL.

`release-server.yml` (GitHub Actions, on `v*` tags) builds the jar and attaches it to a
GitHub Release — it does not itself deploy. The Gradle remote build cache
(`settings.gradle`) is a *separate* DigitalOcean Spaces bucket (`projektorcache`) used only
for CI build caching, unrelated to app deployment.

## Repo-root conventions

- `buildSrc/` — one custom shared Gradle task,
  `UIRestUrlVerificationTask.kt` (verifies UI REST URL usage against server routes).
- `example-data/` — real small sample projects (`spock-gradle`, `kover-gradle`) with test/
  coverage output, used for local dev/demo and exercised by
  `.github/workflows/example-projects.yml`.
- `gradle.properties` — no version catalog; plain properties (`kotlin_version`,
  `jacksonVersion`, etc.) referenced via Groovy `$var` interpolation in each module's
  `build.gradle`.
- `CHANGELOG.md` — hand-maintained per release tag, not auto-generated from commits/PRs.
