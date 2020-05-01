# Projektor publishing script for Node

Add the NPM package as a dev dependency with either NPM:

`npm install --save-dev projektor-publish`

or Yarn

`yarn add -D projektor-publish`

Then run this script to publish test results to the Projektor server:

`yarn projektor-publish --serverUrl=<projektor_url> <file_globs>`

You can include multiple file globs, such as:

`yarn projektor-publish --serverUrl=<url> ui/test-results/*.xml ui/cypress/*.xml`

Another configuration option is to store the config in `projektor.json` in the root of your project.
Then you don't need to specify the parameters on the command line each time.

```
{
  "serverUrl": "<url>",
  "results": [
    "ui/test-results/*.xml",
    "ui/cypress/*.xml"
  ]
}
```

You can also customize the name of the config file by passing the `--configFile=<path>` flag:

`yarn projektor-publish --configFile=projektor.cypress.json`

## Attachments

You can also attach files to the test results, which can be helpful to attach things like screenshots
from Cypress tests to debug test failures in CI where you can't watch the test run.

Use the `attachments` config value to tell Projektor which files to attach.

For example, from the config file:

```
{
  "serverUrl": "<url>",
  "results": [
    "ui/test-results/*.xml",
    "ui/cypress/*.xml"
  ],
  "attachments": [
     "ui/cypress/screenshots/*.png"
  ]
}
```

Or from the command line: 

`yarn projektor-publish --serverUrl=<projektor_url> --attachments="ui/cypress/screenshots/*.png" ui/cypress/*.xml`

## Exit code when there is a test failure

When running in CI systems it can be handy to combine the execution of tests like Cypress with
the publishing of results to Projektor by using the "or" `||` operator.
Then the results will be published even when the tests fail if the CI system would normally stop the build execution when the tests fail.

For example, `yarn cy:run || yarn projektor-publish`

A downside of this approach is that due to the `||` the exit code is `0` and the CI build won't stop after publishing the test results,
as you'd probably want it to when tests fail.

To support using this approach but stopping the CI build from proceeding if there is a test failure, you can use the `exitWithFailure` configuration flag.

For example, if configuring Projektor via the command line: `yarn cy:run || yarn projektor-publish --serverUrl=<projektor_url> --exitWithFailure <file_globs>`

Or in the configuration file:

```
{
  "serverUrl": "<url>",
  "results": [
    "ui/test-results/*.xml"
  ],
  "exitWithFailure": true
}
```

## Changelog

* 2.2.0
  * Added `exitWithFailure` configuration option
