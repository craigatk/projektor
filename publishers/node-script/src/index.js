function run(args, publishToken, defaultConfigFilePath) {
  const argv = require("minimist")(args, { boolean: "exitWithFailure" });
  const fs = require("fs");
  const { collectAndSendResults } = require("./publish");

  let serverUrl;
  let resultsFileGlobs;
  let attachmentFileGlobs;
  let exitWithFailure;

  const configFilePath = argv.configFile || defaultConfigFilePath;

  if (fs.existsSync(configFilePath)) {
    const configFileContents = fs.readFileSync(configFilePath);
    const config = JSON.parse(configFileContents);

    serverUrl = config.serverUrl;
    resultsFileGlobs = config.results;
    attachmentFileGlobs = config.attachments;
    exitWithFailure = config.exitWithFailure;
  } else {
    serverUrl = argv.serverUrl;
    resultsFileGlobs = argv._;
    if (argv.attachments) {
      attachmentFileGlobs = [argv.attachments];
    }
    exitWithFailure = argv.exitWithFailure;
  }

  if (resultsFileGlobs) {
    const resultsBlob = collectAndSendResults(
      serverUrl,
      publishToken,
      resultsFileGlobs,
      attachmentFileGlobs
    );

    if (exitWithFailure && containsTestFailure(resultsBlob)) {
      console.log(
        "Projektor exiting with non-zero exit code due to test failure"
      );
      process.exitCode = 1;
    }
  } else {
    console.error(
      `Results files not configured, please specify them either on the command line or in the ${configFilePath} config file`
    );
  }
}

function containsTestFailure(resultsBlob) {
  return resultsBlob.indexOf("<failure") != -1;
}

module.exports = {
  run,
};
