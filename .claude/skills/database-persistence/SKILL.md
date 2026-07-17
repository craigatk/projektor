---
name: database-persistence
description: Use when adding/changing database schema (Flyway migrations under server/persistence/database-schema), writing or modifying a *Repository/*DatabaseRepository class, regenerating jOOQ code, or writing tests that touch the database. Covers the migration → jOOQ codegen workflow, the repository pattern (interface + *DatabaseRepository backed by jOOQ DSLContext/DAOs), Koin wiring, transaction handling, and how the real-Postgres test setup works (per-test HikariCP pool, no truncation between tests, random-ID isolation).
user-invocable: true
---

# Working with Projektor's database persistence layer

Relational persistence (Postgres via jOOQ) lives in `server/persistence/database-schema`
(migrations + generated code) and is consumed by feature-specific `*Repository`/
`*DatabaseRepository` pairs under `server/server-app/src/main/kotlin/projektor/<feature>/`.
`server/persistence/object-store` is a **separate, unrelated** module (MinIO/S3 blob storage
for attachments) — don't conflate the two "persistence" concerns.

## Changing the schema: migration → jOOQ codegen

1. Add a new file to `server/persistence/database-schema/src/main/resources/db/migration/`,
   named `V<N>__Description.sql` (next sequential integer, double underscore, no leading
   zeros — check the highest existing `V<N>` first). One logical change per file.
2. Regenerate jOOQ code: `./gradlew :server:persistence:database-schema:generateJooq`.
   **The database must be up first** (`docker-compose up -d postgres` at repo root) — the
   task depends on `flywayMigrate`, which runs your new migration against a live Postgres at
   `jdbc:postgresql://localhost:5433/projektordb` before introspecting the schema.
3. **Commit the generated diff.** Generated code lives at
   `server/persistence/database-schema/src/main/java/projektor/database/generated/` and is
   checked into git (not gitignored) — `Tables.java`, plus `tables/`, `tables/records/`,
   `tables/pojos/`, `tables/daos/` per table. Review the diff like any other generated-code
   change; a migration without a matching regenerated-and-committed diff means the repo
   layer is still compiling against the old schema shape.
4. `flywayMigrate` also runs automatically at app startup (`Application.kt`, via
   `DataSourceConfig.flywayMigrate`) and at the top of every DB-touching test (see below) —
   so in practice your migration gets exercised constantly; you don't need a separate "did
   it apply" check beyond running the test suite.

`validateMigrationNaming(true)` is set on the Flyway config, so a misnamed file
(`V41_Foo.sql` missing the double underscore, non-numeric version, etc.) fails fast at
startup/test-time rather than being silently skipped.

## Repository pattern

One interface + one jOOQ-backed implementation per feature, e.g.
`projektor/testrun/TestRunRepository.kt` (interface, `suspend fun`s, domain types) +
`TestRunDatabaseRepository.kt` (impl). There's no shared abstract base class — each impl is
a plain class taking a single `DSLContext` constructor param:

```kotlin
class TestRunDatabaseRepository(private val dslContext: DSLContext) : TestRunRepository {
    override suspend fun fetchTestRun(publicId: PublicId): TestRun? = withContext(Dispatchers.IO) {
        dslContext.select(...).from(TEST_RUN).where(TEST_RUN.PUBLIC_ID.eq(publicId.id))
            .fetchOneInto(TestRun::class.java)
    }
}
```

Conventions to follow:
- Wrap every method body in `withContext(Dispatchers.IO) { ... }` — JDBC calls are blocking.
- Import generated table constants directly (`import projektor.database.generated.Tables.TEST_RUN`)
  and use the bare constant (`TEST_RUN`, not `Tables.TEST_RUN`) in query bodies.
- Alias generated POJOs that collide with domain API class names of the same simple name,
  e.g. `import projektor.database.generated.tables.pojos.TestRun as TestRunDB`.
- Simple reads: jOOQ DSL directly (`.select().from().where().fetchOneInto(...)` /
  `.fetchInto(...)`).
- Joined/nested reads: `.fetchResultSet()` piped through a SimpleFlatMapper
  `JdbcMapperFactory` mapper (see `testRunMapper` in `TestRunDatabaseRepository` for the
  `.addKeys(...)`/`.ignorePropertyNotFound()` pattern) — use the `addPrefixToFields`
  extension (`projektor.util.JooqUtil`) to alias a joined table's columns before flattening.
- Writes: generated **DAOs** (`TestRunDao(configuration)`, `.insert(pojo)`, `.update(pojo)`,
  `.deleteById(...)`, `.fetchOneByPublicId(...)`), not hand-written INSERT/UPDATE DSL, unless
  the operation doesn't map cleanly to a DAO method (e.g. `.onDuplicateKeyUpdate()` upserts).
- **Multi-step writes must go through a transaction**, and DAOs/queries inside the block must
  be built from the block's `configuration` parameter — not the outer `dslContext` — to
  actually participate:
  ```kotlin
  dslContext.transaction { configuration ->
      val testRunDao = TestRunDao(configuration)
      testRunDao.insert(testRunPojo)
      // ... more DAOs built from the same `configuration`
  }
  ```
  Building a DAO from the outer `dslContext` instead of the transaction's `configuration` is
  the most common way to silently defeat a transaction — double check this when reviewing or
  writing multi-table writes.

**Koin wiring** — all in one place, `server/server-app/src/main/kotlin/projektor/AppModule.kt`,
`createAppModule(...)`. Bind the interface to the impl, `get()` resolves the singleton
`DSLContext`:
```kotlin
single { dslContext }
single<TestRunRepository> { TestRunDatabaseRepository(get()) }
```
Consumers pull it with `by inject()` (app layer) or constructor injection resolved via Koin
(services).

## Schema-name indirection (non-obvious)

Generated code is always compiled against the `public` schema, but the runtime schema is
configurable via `DB_SCHEMA`. `DataSourceConfig.createDSLContext` remaps this at the jOOQ
`Settings` level:
```kotlin
Settings().withRenderMapping(
    RenderMapping().withSchemata(
        MappedSchema().withInput("public").withOutput(dataSourceConfig.schema)
    )
)
```
If queries seem to hit the wrong schema in a non-default-schema deployment, this remapping —
not the generated table constants — is the place to look.

`DataSourceConfig` also auto-converts PaaS-style `postgres://user:pass@host:port/db` URIs
(DigitalOcean/Heroku) into the `jdbc:postgresql://...` form jOOQ/Hikari expect, so `DB_URL`
can be set to either format directly.

## Tests: real Postgres, no mocks, no truncation

Two base classes, both hit a real database — there's no in-memory/mocked DB layer:
- `DatabaseRepositoryTestCase` — repository-level tests.
- `ApplicationTestCase` — full HTTP-level tests (Ktor `testApplication`).

Each test method gets its **own fresh HikariCP pool** (`@BeforeEach`) pointed at
`jdbc:postgresql://localhost:5433/projektordb` (`testuser`/`testpass`), overridable via
`DB_URL`/`DB_USERNAME`/`DB_PASSWORD` env vars — CI uses port 5432 instead of 5433 (Postgres
as a GitHub Actions service vs. local docker-compose), always driven by config, never
hardcoded beyond the local default. This is why a big test run shows dozens of
`HikariPool-N - Starting...` lines; it's expected, not a leak. `flywayMigrate` runs again on
every test (idempotent — no-op if already applied) and the full Koin module graph is rebuilt
per test (`stopKoin()` / `startKoin { modules(createAppModule(...)) }`).

**There is no cleanup/truncate between tests.** The database accumulates rows across a whole
test run (and across CI runs against the same service). Isolation instead comes from every
test generating a random public ID / org / repo name and filtering all fetches by it:
```kotlin
val publicId = randomPublicId()               // projektor.incomingresults.RandomPublicId
val orgName = RandomStringUtils.randomAlphabetic(12)
```
When writing a new DB test, follow this — don't assume a clean table, and don't add manual
cleanup logic; generate unique keys instead, matching the rest of the suite.

**Fixture data**: use `TestRunDBGenerator` (a test field, `testRunDBGenerator`, already
wired up in `DatabaseRepositoryTestCase`) rather than hand-rolling inserts for anything
resembling a full test run — `createTestRun(...)`, `createTestRunInRepo(...)`,
`createSimpleTestRun(...)`, `createSimpleFailingTestRun(...)`, `addGitMetadata(...)`, etc.
build realistic rows via the generated DAOs. Hand-rolled `dao.insert(pojo)` calls are fine
for narrow, single-table test setup that doesn't fit the generator's shape.

## Local dev commands

| Goal | Command |
|---|---|
| Start Postgres (+ MinIO) for local dev/codegen | `docker-compose up -d postgres` (add `objectstorage` too if touching attachments) |
| Regenerate jOOQ after a migration | `./gradlew :server:persistence:database-schema:generateJooq` |
| Run one repository's tests | `./gradlew :server:server-app:test --tests "projektor.testrun.*"` |
| Run the full server-app suite | `./gradlew :server:server-app:test` |
