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
    resultsFileGlobs = config.results;
    attachmentFileGlobs = config.attachments;
  } else {
    serverUrl = argv.serverUrl;
    resultsFileGlobs = argv._;
    if (argv.attachments) {
      attachmentFileGlobs = [argv.attachments];
    }
  }

  if (resultsFileGlobs) {
    collectAndSendResults(
      serverUrl,
      publishToken,
      resultsFileGlobs,
      attachmentFileGlobs
    );
  } else {
    console.error(
      `Results files not configured, please specify them either on the command line or in the ${configFilePath} config file`
    );
  }
}

module.exports = {
  run,
};
