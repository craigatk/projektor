# Server

The backend server that parses the incoming JUnit XML files & code coverage reports, persists them,
then makes them available in an API to the UI.

## Development

### Running locally

First, start up a local Postgres database and Minio object store server.
There is a `docker-compose.yml` file in the root of the repo
that includes one, you can start it with `docker-compose up`

Then start the ktor app with `../../gradlew run`
This will start the app on `http://localhost:8080`

#### Running with object-store persistence

The server can store its data in an S3-compatible object store instead of Postgres
(see [the object-store persistence plan](../persistence/OBJECT_STORE_PLAN.md)).
To run it against the docker-compose object store:

```shell
PERSISTENCE_TYPE=object-store \
PERSISTENCE_OBJECT_STORE_URL=http://localhost:9000 \
PERSISTENCE_OBJECT_STORE_BUCKET_NAME=projektorlocal \
PERSISTENCE_OBJECT_STORE_ACCESS_KEY=minio_access_key \
PERSISTENCE_OBJECT_STORE_SECRET_KEY=minio_secret_key \
PERSISTENCE_OBJECT_STORE_AUTO_CREATE_BUCKET=true \
../../gradlew run
```

`PERSISTENCE_OBJECT_STORE_KEY_PREFIX` optionally puts all keys under a prefix, so the data can share a bucket.
The object store must support conditional writes (`If-Match` / `If-None-Match`), which MinIO, RustFS and
AWS S3 do. The credentials need to read, write, list and delete objects in the bucket; startup fails otherwise.

Test run cleanup doesn't remove everything, so set these bucket lifecycle rules in production (under the key prefix,
if there is one):

* Expire `failures/`, which holds the full body of every upload that failed to process, after as long as those are
  useful for debugging.
* Expire `processing/`, which holds each upload's status, after longer than `MAX_REPORT_AGE_DAYS` so the UI
  can still say a test run was cleaned up.
* Abort incomplete multipart uploads after a day or so. Attachments over 10MB are uploaded in parts, and a server that
  stops partway leaves the parts behind.

The server processes uploads after accepting them. When asked to stop, it waits up to
`PROCESSING_SHUTDOWN_TIMEOUT_SECONDS` (default 25) for them to finish, so set this below how long your host waits before
killing the server. Uploads still processing after that, or when the server is killed, are lost.

## Stack

### Server

* Kotlin language
* Ktor framework
* jOOQ for database access

### Testing

* kotlintest for data-driven tests
* JUnit for ktor application tests
* Strikt for assertions
