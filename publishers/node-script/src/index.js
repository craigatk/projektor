function run(args, publishToken, defaultConfigFilePath) {
  const argv = require("minimist")(args);
  const fs = require("fs");
  const { collectAndSendResults } = require("./publish");

  let serverUrl;
  let resultsFileGlobs;
  let attachmentFileGlobs;

  const configFilePath = argv.configFile || defaultConfigFilePath;

  if (fs.existsSync(configFilePath)) {
    const configFileContents = fs.readFileSync(configFilePath);
    const config = JSON.parse(configFileContents);

    serverUrl = config.serverUrl;
    resultsFileGlobs = config.resultsFileGlobs;
    attachmentFileGlobs = config.attachments;
  } else {
    serverUrl = argv.serverUrl;
    resultsFileGlobs = argv._;
  }

  collectAndSendResults(serverUrl, publishToken, resultsFileGlobs, attachmentFileGlobs);
}

module.exports = {
  run
};
