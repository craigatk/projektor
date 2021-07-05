# Projektor development

## Tech stack

Projektor is composed of a backend [ktor](https://ktor.io/) server written in [Kotlin](https://kotlinlang.org/) and
a frontend [React](https://reactjs.org/) app written in Typescript/Javascript.

### Server

See the [server app readme](server/server-app/README.md) for more details.

### UI

See the [UI app readme](ui/README.md) for more details.

## Deployment

### Packaging

While the UI and server are separated into their own projects and run independently during
development for ease of development, to reduce the amount of work needed
to deploy the UI files are packaged together as part of the server app into a single JAR file.

To build the package execute:

`./gradlew :server:server-app:assembleFull`

### Heroku

The server app project has the Heroku Gradle plugin and config
to deploy the app to Heroku using the JVM buildpack.

First, change the app name in the `heroku` block in `server/server-app/build.gradle` to your Heroku app name.

Then run the deployment with `./gradlew :server:server-app:deployHeroku`
