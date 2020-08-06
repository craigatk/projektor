async function runCLI(cliArgs, publishToken, defaultConfigFilePath) {
  const parsedArgs = require("minimist")(cliArgs, {
    boolean: "exitWithFailure",
  });

  parsedArgs.resultsFileGlobs = parsedArgs._;

  return run(parsedArgs, publishToken, defaultConfigFilePath);
}

async function run(args, publishToken, defaultConfigFilePath) {
  const fs = require("fs");
  const { collectAndSendResults } = require("./publish");
  const { writeSlackMessageFileToDisk } = require("./slack");

  let serverUrl;
  let resultsFileGlobs;
  let attachmentFileGlobs;
  let exitWithFailure;
  let writeSlackMessageFile;
  let slackMessageFileName;
  let projectName;

  const configFilePath = args.configFile || defaultConfigFilePath;

  if (fs.existsSync(configFilePath)) {
    const configFileContents = fs.readFileSync(configFilePath);
    const config = JSON.parse(configFileContents);

    serverUrl = config.serverUrl;
    resultsFileGlobs = config.results;
    attachmentFileGlobs = config.attachments;
    exitWithFailure = config.exitWithFailure;
    writeSlackMessageFile = config.writeSlackMessageFile;
    slackMessageFileName = config.slackMessageFileName;
    projectName = config.projectName;
  } else {
    serverUrl = args.serverUrl;
    resultsFileGlobs = args.resultsFileGlobs;

    if (args.attachments) {
      attachmentFileGlobs = Array.isArray(args.attachments)
        ? args.attachments
        : [args.attachments];
    }
    exitWithFailure = args.exitWithFailure;
    writeSlackMessageFile = args.writeSlackMessageFile;
    slackMessageFileName = args.slackMessageFileName;
    projectName = args.projectName;
  }

  if (resultsFileGlobs) {
    const { resultsBlob, reportUrl, publicId } = await collectAndSendResults(
      serverUrl,
      publishToken,
      resultsFileGlobs,
      attachmentFileGlobs
    );

    if (writeSlackMessageFile) {
      writeSlackMessageFileToDisk(
        reportUrl,
        slackMessageFileName || "projektor_failure_message.json",
        projectName
      );
    }

    if (exitWithFailure && containsTestFailure(resultsBlob)) {
      console.log(
        "Projektor exiting with non-zero exit code due to test failure"
      );
      process.exitCode = 1;
    }

    return { reportUrl, publicId };
  } else {
    console.error(
      `Results files not configured, please specify them either on the command line or in the ${configFilePath} config file`
    );

    return { reportUrl: null, publicId: null };
  }
}

function containsTestFailure(resultsBlob) {
  return resultsBlob.indexOf("<failure") != -1;
}

module.exports = {
  runCLI,
  run,
};
