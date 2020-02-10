function run(args, defaultConfigFilePath) {
  const argv = require("minimist")(args);
  const fs = require("fs");
  const { collectAndSendResults } = require("./publish");

  let serverUrl;
  let resultsFileGlobs;

  const configFilePath = argv.configFile || defaultConfigFilePath;

  if (fs.existsSync(configFilePath)) {
    const configFileContents = fs.readFileSync(configFilePath);
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
