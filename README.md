# Projektor

Tests failing on your machine and need help debugging them? Or tests are passing local but failing in CI and
CI doesn't record the test report?
Access and share your test reports quickly and easily with Projektor.

## How it works

Many testing frameworks support writing their results in JUnit's XML format.
To support showing results from the most types of testing frameworks, Projektor parses these
JUnit XML results, saves them in a Postgres database, then makes them available to you and your
team with a simple, shareable URL.

Simply send your JUnit XML results from whichever testing framework you use to the `/results`
endpoint in your Projektor server. Or use one of the convenient publishing plugins to
to automatically publish your test results as part of your test executions.
Then you will get back the URL to view your results.

## Configuration

### Database

Projektor stores the parsed test results in a Postgres database.
To configure which database Projektor connects to, please set the following environment variables:

```
DB_USERNAME=<username>
DB_PASSWORD=<password>
DB_URL=<url>
```

For example, to connect to a local database:

```
DB_USERNAME=testuser
DB_PASSWORD=testpass
DB_URL=jdbc:postgresql://localhost:5433/projektordb
```

## Publishing plugins

Projektor includes two publishing plugins to make publishing your test reports easier,
one for Gradle and the other for Javascript/Node.js

### Gradle

Please see the [Gradle plugin readme for more details on installation and configuration](publishers/gradle-plugin/README.md)

### Javascript/Node

Please see the [Node script readme for more details on installation and configuration](publishers/node-script/README.md)

## Development

For information on how Projektor is developed, how to build it from source and deploy it yourself, and other
development information, please see [the development guide](DEVELOPMENT.md)
