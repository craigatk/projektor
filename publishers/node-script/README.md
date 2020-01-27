# Projektor publishing script for Node

Add the NPM package as a dev dependency with either NPM:

`npm install --save-dev projektor-publish`

or Yarn

`yarn add -D projektor-publish`

Then run this script to publish test results to the Projektor server:

`yarn projektor-publish --serverUrl=<specviewer_url> <file_globs>`

You can include multiple file globs, such as:

`yarn projektor-publish --serverUrl=<url> ui/test-results/*.xml ui/cypress/*.xml`
