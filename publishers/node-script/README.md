# Projektor node-script

Add the NPM package as a dev dependency with either NPM:

`npm install --save-dev @projektor/publish`

or Yarn

`yarn add -D @projektor/publish`

Then run this script to publish test results to the Projektor server:

`node index.js --serverUrl=<specviewer_url> <file_globs>`

You can include multiple file globs, such as:

`node src/index.js --serverUrl=<url> ui/test-results/*.xml ui/cypress/*.xml`
