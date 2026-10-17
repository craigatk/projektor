# Plan: object-store backend for server persistence

## Where things stand

- The persistence layer is 17 repository interfaces, each with one `*DatabaseRepository` built on jOOQ, wired in
  `AppModule.kt`. Services depend only on the interfaces, so a second backend fits behind them.
- `server/persistence/object-store` already wraps MinIO for attachments (put, get, remove, bucket operations), but has
  no list and no conditional write.
- Things tied to Postgres outside the repository interfaces:
  - Internal row IDs in interface signatures: `testRunId: Long` in `TestRunRepository`, `TestRunMetadataRepository`,
    `PerformanceResultsRepository` and `CodeQualityReportRepository`, with values coming from
    `GroupedTestResultsService`, `AppendTestResultsService` and `ParsedResultsToDBMapper`.
  - Scheduler lock: `SchedulerLock` uses ShedLock's `JdbcTemplateLockProvider` on a `DataSource`.
  - Startup: `Application.kt` always creates the datasource, runs Flyway, and builds `AttachmentDatabaseRepository`.
  - Tests: ~37 specs use `DatabaseRepositoryTestCase` and ~100 use `ApplicationTestCase`; both seed and assert data
    through jOOQ DAOs.

There are two kinds of access pattern:

1. Reads for a single run, keyed by `PublicId`: test run, suites, cases, system out/err, coverage files, attachments,
   performance results, quality reports, processing status. These map naturally onto objects under `runs/{publicId}/`.
2. Queries across runs by repo, project, branch or PR: timelines, recent summaries, failing cases across runs, test
   case history and duration baselines, previous mainline run, latest run for a PR, org repos with coverage, grouped
   run lookup, cleanup queries. An object store can't query, so these need index objects that we maintain ourselves.

## Design

### Backend selection

New config key `persistence.type` (env var `PERSISTENCE_TYPE`): `database` (default) or `object-store`. With
`object-store` there is no datasource, no Flyway and no jOOQ. Separate settings point the backend at its own bucket and
key prefix: `PERSISTENCE_OBJECT_STORE_URL` / `_BUCKET` / `_ACCESS_KEY` / `_SECRET_KEY` / `_PREFIX`.

### Key layout

All payloads are JSON, gzipped where large.

```
runs/{publicId}/run.json                 # summary, results metadata, git metadata, system attributes (pinned), group
runs/{publicId}/suites/{idx}.json        # suite + its test cases + failures (written once, never mutated)
runs/{publicId}/suites/{idx}/out.txt|err.txt and cases/{caseIdx}/out.txt|err.txt
runs/{publicId}/coverage/run.json        # overall stats + group list
runs/{publicId}/coverage/groups/{name}.json   # group stats + files
runs/{publicId}/performance.json, quality.json, attachments.json
processing/{publicId}.json               # results_processing status/error
failures/{invTs}-{publicId}.json         # processing_failure (newest-first by listing)

repos/{org}/{repo}/{project|_}/timeline/{yyyy-MM}.json   # monthly shard of run index entries
repos/{org}/{repo}/{project|_}/pr/{n}/{invTs}-{publicId} # empty marker objects
repos/{org}/{repo}/{project|_}/slack.json, current-coverage.json
groups/{org}/{repo}/{group}              # -> publicId
created/{yyyy-MM-dd}/{publicId}          # cleanup index (carries pinned/hasAttachments flags)
locks/{lockName}.json                    # scheduler lease
```

- `invTs` is `Long.MAX_VALUE - epochMillis`, zero-padded. S3 lists keys lexicographically, so a prefix listing returns
  the newest runs first.
- Each timeline shard entry holds what cross-run queries need: publicId, timestamp, branch, branch type, CI flag, PR
  number, summary counts and durations, coverage %, and whether the run has coverage/performance data. Timelines,
  recent summaries, recent IDs, counts and previous-run lookups then read one or two shard objects instead of
  thousands of run objects.
- History, baseline and failing-case queries take the newest N matching entries from the shards, then fetch those
  runs' suite objects in parallel with coroutines.

### Consistency without transactions

- Most data is write-once: a suite object never changes after it's written.
- Mutable shared objects (`run.json`, timeline shards, coverage run/group objects) are updated with read-modify-write
  plus a conditional PUT (`If-Match` on the ETag, or `If-None-Match: *` when creating), retried on 412. Supported by
  MinIO and AWS S3 (since late 2024).
- Write order: payload objects first, then `run.json`, then index entries. A crash can leave orphaned payloads
  (harmless, cleaned up later) but never index entries pointing at missing data.

### Scheduler lock

A small ShedLock `LockProvider` that takes a lease with an `If-None-Match` PUT on `locks/{name}.json` and steals the
lease once expired. `SchedulerLock` accepts a `LockProvider` instead of building one from a `DataSource`.

## Implementation phases

### Phase 0: separate the interfaces from Postgres (DB backend only, no behavior change) — DONE

1. Replaced `testRunId: Long` with `PublicId` in `TestRunRepository`, `TestRunMetadataRepository`,
   `PerformanceResultsRepository` and `CodeQualityReportRepository`; `saveGroupedTestRun` / `appendTestSuites` no
   longer return row IDs. DB implementations resolve the ID via `projektor.util.fetchTestRunId`.
2. Added `projektor.persistence`: `Persistence` (owns startup/shutdown and provides a Koin module),
   `DatabasePersistence` + `createDatabasePersistenceModule`, and `PersistenceConfig` (`ktor.persistence.type` /
   `PERSISTENCE_TYPE`, only `database` supported so far). `createAppModule` no longer takes a datasource or
   DSLContext, and `SchedulerLock` takes a ShedLock `LockProvider`.
3. `deleteTestRun` contract documented on the interface: it deletes data owned by the run; coverage, attachments and
   processing status are deleted by `TestRunCleanupService` through their own repositories (already the case), so
   no Postgres cascade behavior leaks into the contract.

### Phase 1: object-store primitives (`server/persistence/object-store`) — DONE

- `ObjectStoreClient`: `putObject(bytes, contentType, WriteCondition)` (None / IfNotExists / IfMatch, throws
  `PreconditionFailedException` on 412/409), `getObjectWithETag`, `listObjects(prefix, delimiter, startAfter,
  limit)` returning objects + common prefixes, `removeObjectsWithPrefix`, and `removeObjects` batched to 1000 keys.
- `projektor.objectstore.json.JsonObjectStore`: Jackson documents under a key prefix; `read`, `readVersioned`,
  `write`, `create`, `replace`, `update(key) { old -> new }` (CAS loop with jittered exponential backoff, throws
  `ConcurrentUpdateException` after max attempts), `delete`, `deletePrefix`, `list`. Documents over 8KB are
  gzipped; reads detect gzip by magic bytes.
- JUnit tests against the local docker-compose object store (RustFS, which honors `If-Match` / `If-None-Match`),
  including 8 threads x 10 concurrent updates losing nothing.
- Not done yet: generic collection types (`TypeReference`) for documents. Repositories wrap lists in data classes
  for now.

### Phase 2: single-run repositories — DONE

- `CoverageRepository` no longer exposes jOOQ POJOs: groups are identified by test run public ID + group name, and
  `addCoverageReport` takes the group's files so a new group and its files are written together.
- Object-store repositories next to their DB counterparts: `TestRun`, `TestSuite`, `TestCase` (single-run methods),
  `TestRunMetadata`, `TestRunSystemAttributes`, `Attachment`, `ResultsProcessing`, `ProcessingFailure`,
  `PerformanceResults`, `CodeQualityReport`, `Coverage`. Cross-run methods (`findTestRunsCreatedBefore…`,
  `findTestRunWithGroup`, test case history and duration baselines) are `TODO()` until Phase 3.
- `projektor.persistence.objectstore`: key layout (`ObjectStoreKeys`), stored document types, `TestRunDocumentStore`
  (shared run reads/writes, parallel writes bounded at 16), `createObjectStorePersistenceModule`.
- Layout as implemented (differs from the original sketch above):
  - `runs/{id}/run.json` holds the summary, metadata, groups and test suites *without* test cases, so
    `fetchTestRun` is one GET.
  - Test cases live in chunk documents `runs/{id}/cases/{firstSuiteIdx}.json` of up to 100 suites each.
  - Output: `runs/{id}/output/suite-{idx}-{out|err}.json` and `case-{suiteIdx}-{caseIdx}-{out|err}.json`.
  - Coverage under `coverage/{id}/` and attachments at `attachments/{id}.json`, outside `runs/{id}/`, so
    `deleteTestRun` is a prefix delete that keeps them for the cleanup service to delete separately.
- Values are normalized like Postgres stores them (numeric(12,3) durations, microsecond timestamps).
- `ObjectStoreParityTest` saves identical input to both backends and asserts every fetch returns equal results;
  `ObjectStoreRepositoriesTest` covers object-store-only behavior (multi-chunk runs, concurrent appends,
  delete scope, failure ordering).
- Parity findings, intentionally not copied by the object store:
  - The DB `fetchTestRun` returns the internal row ID as `summary.id`, the object store returns the public ID.
  - The DB gives concurrent appends to the same run duplicate test suite indexes; the object store reserves
    distinct indexes.
- Parity quirks the object store does copy: appended suites are numbered from `startingIdx + 1` (so the append
  path leaves a gap of one), `fetchHighestTestSuiteIndex` returns 0 when there are no suites, and test cases nested
  in suites don't carry their suite's file name or output flags.

### Phase 3: index maintenance and cross-run repositories — DONE

- `projektor.persistence.objectstore.TestRunIndex`:
  - Repository timeline shards `repos/{urlencoded repo}/timeline/{yyyy-MM}.json`, one entry per run with Git
    metadata (summary, project/branch/main/PR/commit, CI flag, results group, whether coverage was started, summed
    coverage group stats, performance results). Covers what the PR markers and group pointers in the original sketch
    were for, so those weren't needed.
  - Created-date markers `created/{yyyy-MM-dd}/{publicId}.json` for cleanup (includes runs without Git metadata).
    Pinned/attachment status is read per candidate at query time.
  - Organization markers `orgs/{org}/{urlencoded repo}.json`.
  - Entries are rebuilt, never patched: every mutation calls `refreshEntry(publicId)`, which re-reads the run,
    coverage and performance documents *inside* the shard's compare-and-swap so concurrent changes converge.
    A reindex job would be the same call in a loop.
  - Queries read all of a repository's shards and filter in memory. That stays cheap because cleanup bounds the
    months of shards; with cleanup disabled, repository pages get slower as history grows.
- Object-store `RepositoryTestRun`, `RepositoryCoverage` (last-known coverage at
  `repos/{repo}/current-coverage/{project}.json`), `RepositoryPerformance`, `PreviousTestRun`, `OrganizationCoverage`,
  `RepositorySlackChannel` (`repos/{repo}/slack-channel/{project}.json`), test case history and duration baselines,
  `findTestRunWithGroup`, and the cleanup queries. The object-store module now binds every repository.
- `ObjectStoreIndexParityTest` saves a multi-repo, multi-month scenario to both backends and compares every
  cross-run query (cleanup queries compared on the scenario's own runs, since the test database is shared).
  `ParityTest` setup moved into `ObjectStoreParityTestCase`.
- More parity findings:
  - Database bug: the repository performance timeline returns only one performance result per run (its row mapper
    is keyed on public ID). The object store returns all of them.
  - Queries ordered by created timestamp descending have no tiebreaker in SQL; the object store matches the
    database's practical behavior (ties in save order) with a stable sort.
  - Duration baseline medians come back from the database at a scale that depends on the Postgres driver's
    text/binary transfer mode; the object store returns the binary-mode form (`3.0`), and the parity test compares
    medians by value.

### Phase 4: wiring and operations — DONE (except telemetry)

- `PERSISTENCE_TYPE=object-store` selects `ObjectStorePersistence`, configured by `PERSISTENCE_OBJECT_STORE_URL`,
  `_BUCKET_NAME`, `_ACCESS_KEY`, `_SECRET_KEY`, optional `_KEY_PREFIX`, and `_AUTO_CREATE_BUCKET` (otherwise startup
  fails if the bucket doesn't exist). Parsed by `ObjectStorePersistenceConfig`.
- `ObjectStoreLockProvider`: ShedLock leases at `locks/{name}.json`, taken with create-if-missing or replace-if-expired
  conditional writes and released by shortening the lease with an `If-Match` write (so releasing an expired lock
  can't release the instance that took it over).
- `ApplicationTestCaseConfig(objectStorePersistence = true)` runs application tests on the object-store backend;
  `ObjectStorePersistenceApplicationTest` publishes results over HTTP and checks the run, suite, case, history, git
  metadata, coverage, previous run, repository timelines, org coverage and pinning endpoints.
- Health check needed no change (it doesn't touch the database). Local run instructions in `server-app/README.md`.
- Not done: OpenTelemetry spans and Micrometer timers on the object-store repositories. The database backend only
  has spans on a few methods, so this isn't a parity gap, but object-store latency won't be visible in metrics yet.

### Phase 5: tests

- Turn the repository specs into abstract contract specs with a subclass per backend. Seed through the repository
  interfaces where possible; add per-backend fixture helpers where specs currently seed with DAOs.
- Keep the `ApplicationTestCase` tests on the DB backend. Add a smaller application suite with
  `PERSISTENCE_TYPE=object-store` covering publish -> fetch run, suites, cases, coverage, attachments, repo timeline,
  test case history, cleanup, and the MCP failing-test context tool.
- Optional Cypress smoke run against the object-store server.

### Phase 6: docs

New environment variables and limitations in `../projektor-doc`, plus a release note.

## Risks and open questions

- Motivation (dropping Postgres for cost/ops, or something else) decides how hard to push on cross-run performance.
  Very large repo timelines are where this backend is weakest compared with SQL.
- Data migration: is a one-time Postgres -> object store export needed? If so, a CLI task on the Phase 2-3 writers.
- Store compatibility: conditional PUT needs MinIO or AWS S3 (Nov 2024+). GCS S3 interop and some S3-compatible stores
  may not honor `If-Match`; document supported stores.
- Index drift after partial writes: a periodic "reindex repo from `runs/`" job would make it recoverable (optional
  for v1).
