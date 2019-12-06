# Server

The backend server that parses the incoming JUnit XML files, persists them,
then makes them available in an API to the UI.

## Development

### Running locally

First, start up a local Postgres database.
There is a `docker-compose.yml` file in the root of the repo
that includes one, you can start it with `docker-compose up`

Then start the ktor app with `../gradlew run`
This will start the app on `http://localhost:8080`

## Stack

### Server

* Kotlin language
* Ktor framework
* jOOQ for database access

### Testing

* kotlintest for data-driven tests
* JUnit for ktor application tests
* Strikt for assertions
