const { writeNoResultsSlackMessageFileToDisk } = require("./slack");

async function runCLI(cliArgs, publishToken, defaultConfigFilePath) {
  const parsedArgs = require("minimist")(cliArgs, {
    boolean: "exitWithFailure",
  });

  parsedArgs.resultsFileGlobs = parsedArgs._;
  const env = process.env;

  return run(parsedArgs, env, publishToken, defaultConfigFilePath);
}

async function run(args, env, publishToken, defaultConfigFilePath) {
  const fs = require("fs");
  const { collectAndSendResults } = require("./publish");
  const { writeSlackMessageFileToDisk } = require("./slack");
  const { writeResultsFileToDisk } = require("./results-file");
  const _ = require("lodash");

  let serverUrl;
  let resultsFileGlobs;
  let attachmentFileGlobs;
  let coverageFileGlobs;
  let performanceFileGlobs;
  let codeQualityFileGlobs;
  let baseDirectoryPath;
  let exitWithFailure;
  let failOnPublishError;
  let writeSlackMessageFile;
  let slackMessageFileName;
  let slackProjectName;
  let projectName;
  let repositoryName;
  let compressionEnabled;
  let resultsMaxSizeMB;
  let attachmentMaxSizeMB;
  let groupResults;
  let gitMainBranchNames;

  const configFilePath = args.configFile || defaultConfigFilePath;

  if (fs.existsSync(configFilePath)) {
    const configFileContents = fs.readFileSync(configFilePath).toString();
    const config = JSON.parse(configFileContents);

    serverUrl = config.serverUrl;
    resultsFileGlobs = config.results;
    attachmentFileGlobs = config.attachments;
    coverageFileGlobs = config.coverage;
    performanceFileGlobs = config.performance;
    codeQualityFileGlobs = config.codeQuality;
    baseDirectoryPath = config.baseDirectoryPath;

    exitWithFailure = config.exitWithFailure;
    failOnPublishError = config.failOnPublishError;

    writeSlackMessageFile = config.writeSlackMessageFile;
    slackMessageFileName = config.slackMessageFileName;
    slackProjectName = config.slackProjectName;
    repositoryName = config.repositoryName;
    projectName = config.projectName;
    compressionEnabled = config.compressionEnabled;
    resultsMaxSizeMB = config.resultsMaxSizeMB;
    attachmentMaxSizeMB = config.attachmentMaxSizeMB;
    groupResults = config.groupResults;
    gitMainBranchNames = config.gitMainBranchNames;
  } else {
    serverUrl = args.serverUrl;
    resultsFileGlobs = args.resultsFileGlobs;

    if (args.attachments) {
      attachmentFileGlobs = Array.isArray(args.attachments)
        ? args.attachments
        : [args.attachments];
    }
    if (args.coverage) {
      coverageFileGlobs = Array.isArray(args.coverage)
        ? args.coverage
        : [args.coverage];
    }
    if (args.performance) {
      performanceFileGlobs = Array.isArray(args.performance)
        ? args.performance
        : [args.performance];
    }
    if (args.codeQuality) {
      codeQualityFileGlobs = Array.isArray(args.codeQuality)
        ? args.codeQuality
        : [args.codeQuality];
    }
    baseDirectoryPath = args.baseDirectoryPath;

    exitWithFailure = args.exitWithFailure;
    failOnPublishError = args.failOnPublishError;

    writeSlackMessageFile = args.writeSlackMessageFile;
    slackMessageFileName = args.slackMessageFileName;
    slackProjectName = args.slackProjectName;
    repositoryName = args.repositoryName;
    projectName = args.projectName;
    compressionEnabled = args.compressionEnabled;
    resultsMaxSizeMB = args.resultsMaxSizeMB;
    attachmentMaxSizeMB = args.attachmentMaxSizeMB;
    groupResults = args.groupResults;

    if (args.gitMainBranchNames) {
      gitMainBranchNames = Array.isArray(args.gitMainBranchNames)
        ? args.gitMainBranchNames
        : args.gitMainBranchNames.split(",");
    }
  }

  if (_.isNil(compressionEnabled)) {
    compressionEnabled = true;
  }

  if (!_.isNil(attachmentMaxSizeMB)) {
    attachmentMaxSizeMB = parseFloat(attachmentMaxSizeMB);
  } else {
    attachmentMaxSizeMB = 20;
  }

  if (!_.isNil(resultsMaxSizeMB)) {
    resultsMaxSizeMB = parseFloat(resultsMaxSizeMB);
  } else {
    resultsMaxSizeMB = 20;
  }

  if (_.isNil(gitMainBranchNames)) {
    gitMainBranchNames = ["main", "master"];
  }

  const shouldPublish =
    resultsFileGlobs || performanceFileGlobs || codeQualityFileGlobs;

  if (shouldPublish) {
    // https://go-vela.github.io/docs/concepts/pipeline/steps/environment/
    // https://docs.github.com/en/actions/reference/environment-variables

    const isCI = Boolean(env.CI) && env.CI !== "false";
    const gitRepoName =
      repositoryName ||
      env.VELA_REPO_FULL_NAME ||
      env.GITHUB_REPOSITORY ||
      env.DRONE_REPO;
    const gitBranchName = findGitBranchName(env);
    const gitCommitSha = env.VELA_BUILD_COMMIT || env.GITHUB_SHA;
    const gitPullRequestNumber = findGitPullRequestNumber(env);
    const buildNumber = env.VELA_BUILD_NUMBER || env.GITHUB_RUN_NUMBER;

    let group = null;

    if (groupResults && buildNumber) {
      group = buildNumber;
    }

    const {
      resultsBlob,
      performanceResults,
      reportUrl,
      publicId,
      error: publishError,
    } = await collectAndSendResults(
      serverUrl,
      publishToken,
      resultsFileGlobs,
      attachmentFileGlobs,
      coverageFileGlobs,
      performanceFileGlobs,
      codeQualityFileGlobs,
      gitRepoName,
      gitBranchName,
      gitCommitSha,
      gitPullRequestNumber,
      projectName,
      isCI,
      group,
      compressionEnabled,
      baseDirectoryPath,
      resultsMaxSizeMB,
      attachmentMaxSizeMB,
      gitMainBranchNames
    );

    const hasResults = resultsBlob || performanceFileGlobs;

    if (!hasResults) {
      console.log(
        `No test results files found in locations ${resultsFileGlobs}`
      );
    }

    if (performanceFileGlobs && performanceResults.length === 0) {
      console.log(
        `No performance results files found in locations ${performanceFileGlobs}`
      );
    }

    if (isCI) {
      writeResultsFileToDisk(publicId, reportUrl, "projektor_report.json");
    }

    if (writeSlackMessageFile) {
      const messageFileName =
        slackMessageFileName || "projektor_failure_message.json";
      const messageProjectName = slackProjectName || projectName;

      if (hasResults) {
        writeSlackMessageFileToDisk(
          reportUrl,
          messageFileName,
          messageProjectName,
          containsTestFailure(resultsBlob)
        );
      } else {
        writeNoResultsSlackMessageFileToDisk(
          messageFileName,
          messageProjectName
        );
      }
    }

    if (publishError && failOnPublishError) {
      process.exitCode = 1;
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

function printLinkFromFile(resultsFileName) {
  const fs = require("fs");
  const {
    defaultResultsFileName,
    readResultsFileFromDisk,
  } = require("./results-file");

  const fileName = resultsFileName || defaultResultsFileName;

  if (fs.existsSync(fileName)) {
    const results = readResultsFileFromDisk(fileName);

    console.log(`View Projektor results at ${results.reportUrl}`);

    return results.reportUrl;
  } else {
    console.log(`No Projektor results file found with name ${fileName}`);
    return null;
  }
}

function containsTestFailure(resultsBlob) {
  return resultsBlob.indexOf("<failure") !== -1;
}

function findGitBranchName(env) {
  const branchName = env.VELA_PULL_REQUEST_SOURCE || env.DRONE_COMMIT_BRANCH;
  if (branchName) {
    return branchName;
  } else {
    const gitRef = env.VELA_BUILD_REF || env.GITHUB_REF;
    const gitBranchParts = gitRef ? gitRef.split("/") : [];

    // refs/head/branch-name
    return gitBranchParts.length === 3 ? gitBranchParts[2] : null;
  }
}

function findGitPullRequestNumber(env) {
  const pullRequestNumberFromEnv =
    env.VELA_BUILD_PULL_REQUEST || env.DRONE_PULL_REQUEST;

  if (pullRequestNumberFromEnv) {
    return parseInt(pullRequestNumberFromEnv);
  } else {
    return null;
  }
}

module.exports = {
  runCLI,
  run,
  printLinkFromFile,
};
