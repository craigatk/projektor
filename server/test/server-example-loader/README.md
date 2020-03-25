# Server example loader

Tool for publishing example test results to a Projektor server.

## Usage

`../../gradlew run`

## Configuration

By default it publishes to a local server running on http://localhost:8080

If you want to publish to a different address, set the `SERVER_URL` environment variable.
For example:

`SERVER_URL=http://myserver ../../gradlew run`
