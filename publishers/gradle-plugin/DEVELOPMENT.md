# Projektor Gradle plugin development

## Testing

This project has unit and integration tests as well as 
full tests of the plugin leveraging [Gradle TestKit](https://docs.gradle.org/current/userguide/test_kit.html)

To run all the tests, run `../../gradlew check`

Some of the tests use [WireMock](http://wiremock.org/) to verify the sending of test results
to the server.

There is also a set of functional tests in `src/functionalTest` that start up
a Projektor server and run the plugin against it.
