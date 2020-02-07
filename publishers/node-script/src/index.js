function run(args, projectConfigPath) {
  const argv = require("minimist")(args);
  const fs = require("fs");
  const { collectAndSendResults } = require("./publish");

  let serverUrl;
  let resultsFileGlobs;

  if (fs.existsSync(projectConfigPath)) {
    const configFileContents = fs.readFileSync(projectConfigPath);
    const config = JSON.parse(configFileContents);

    serverUrl = config.serverUrl;
    resultsFileGlobs = config.resultsFileGlobs;
  } else {
    serverUrl = argv.serverUrl;
    resultsFileGlobs = argv._;
  }

  collectAndSendResults(serverUrl, resultsFileGlobs);
}

module.exports = {
  run
};
