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

The easiest way I've found to deploy this app to Heroku is using Docker.

Follow the steps on this page in the Heroku UI: Deployment -> Container Registry (Heroku CLI)

* Install the [Heroku CLI](https://devcenter.heroku.com/articles/heroku-cli)
* Then login in on the command line 
  * `heroku login`
* Then log in to the container registry
  * `heroku container:login`
* Next push the Docker image to the registry
  * `heroku container:push web`
* And finally deploy the Docker container
  * `heroku container:release web` 