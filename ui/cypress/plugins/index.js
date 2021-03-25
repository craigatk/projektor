// Adapted from https://github.com/cypress-io/cypress/issues/2522#issuecomment-749316813 - big thanks to the Cypress team for this approach!

// 'yarn install -D del' or 'npm install -D del' - https://www.npmjs.com/package/del
const del = require("del");

module.exports = (on, config) => {
  on("after:spec", (spec, results) => {
    if (results.stats.failures === 0 && results.video) {
      // `del()` returns a promise, so it's important to return it to ensure
      // deleting the video is finished before moving on
      return del(results.video);
    }
  });
};
