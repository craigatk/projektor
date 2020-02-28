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
