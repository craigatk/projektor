# Projektor publishing script for Node

Add the NPM package as a dev dependency with either NPM:

`npm install --save-dev projektor-publish`

or Yarn

`yarn add -D projektor-publish`

Then run this script to publish test results to the Projektor server:

`yarn projektor-publish --serverUrl=<specviewer_url> <file_globs>`

You can include multiple file globs, such as:

`yarn projektor-publish --serverUrl=<url> ui/test-results/*.xml ui/cypress/*.xml`

Another configuration option is to store the config in `projektor.json` in the root of your project.
Then you don't need to specify the parameters on the command line each time.

```
{
  "serverUrl": "<url>",
  "resultsFileGlobs": [
    "ui/test-results/*.xml",
    "ui/cypress/*.xml"
  ]
}
```

You can also customize the name of the config file by passing the `--configFile=<path>` flag:

`yarn projektor-publish --configFIle=projektor.cypress.json`
