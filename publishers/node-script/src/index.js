function run(args) {
  const argv = require("minimist")(args);
  const { collectAndSendResults } = require("./publish");

  const serverUrl = argv.serverUrl;
  const resultsFileGlobs = argv._;

  collectAndSendResults(serverUrl, resultsFileGlobs);
}

module.exports = {
  run
};
