const axios = require("axios");
const fs = require("fs");
const path = require("path");
const { gzip } = require("node-gzip");
const { globsToFilePaths } = require("./file-utils");
const { collectCoverage } = require("./publish-coverage");
const { collectAndSendAttachments } = require("./publish-attachments");

const collectResults = (resultsFileGlobs) => {
  let resultsBlob = "";

  const resultsFilePaths = globsToFilePaths(resultsFileGlobs);

  if (resultsFilePaths.length > 0) {
    console.log(
      `Found ${resultsFilePaths.length} test results file(s) in ${resultsFileGlobs}`
    );

    resultsFilePaths.forEach((filePath) => {
      const fileContents = fs.readFileSync(filePath);
      resultsBlob = resultsBlob + fileContents + "\n";
    });
  }

  return resultsBlob;
};

const collectPerformanceResults = (performanceFileGlobs) => {
  const performanceFilePaths = globsToFilePaths(performanceFileGlobs);

  return performanceFilePaths.map((filePath) => {
    const fileContents = fs.readFileSync(filePath).toString();
    const fileName = path.basename(filePath);

    return {
      name: fileName,
      resultsBlob: fileContents,
    };
  });
};

const sendResults = async (
  serverUrl,
  publishToken,
  resultsBlob,
  performanceResults,
  coverageFilePayloads,
  gitRepoName,
  gitBranchName,
  gitCommitSha,
  gitPullRequestNumber,
  projectName,
  isCI,
  group,
  compressionEnabled,
  resultsMaxSizeMB
) => {
  const headers = {};

  if (publishToken) {
    headers["X-PROJEKTOR-TOKEN"] = publishToken;
  }

  const axiosInstance = axios.create({
    headers,
  });

  const groupedTestSuites = resultsBlob
    ? [
        {
          groupName: `${projectName || "Tests"}`,
          testSuitesBlob: resultsBlob,
        },
      ]
    : [];

  const groupedResults = {
    groupedTestSuites,
    performanceResults,
    coverageFiles: coverageFilePayloads || [],
    metadata: {
      git: {
        repoName: gitRepoName,
        branchName: gitBranchName,
        isMainBranch: gitBranchName === "main" || gitBranchName === "master",
        projectName,
        commitSha: gitCommitSha,
        pullRequestNumber: gitPullRequestNumber,
      },
      ci: isCI,
      group,
    },
  };

  const resultsMaxSize = resultsMaxSizeMB * 1024 * 1024;

  const resultsPostConfig = {
    maxBodyLength: resultsMaxSize,
  };

  if (compressionEnabled) {
    resultsPostConfig.headers = {
      "Content-Encoding": "gzip",
    };
  }

  const resp = await axiosInstance.post(
    `${serverUrl}/groupedResults`,
    compressionEnabled
      ? await gzip(JSON.stringify(groupedResults))
      : groupedResults,
    resultsPostConfig
  );

  return resp.data;
};

const createResultsLogMessage = (
  resultsFileGlobs,
  coverageFileGlobs,
  serverUrl
) => {
  let message = `Gathering results from ${resultsFileGlobs}`;

  if (coverageFileGlobs) {
    message += ` and coverage from ${coverageFileGlobs}`;
  }

  message += ` to send to Projektor server ${serverUrl}`;

  return message;
};

const collectAndSendResults = async (
  serverUrl,
  publishToken,
  resultsFileGlobs,
  attachmentFileGlobs,
  coverageFileGlobs,
  performanceFileGlobs,
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
  attachmentMaxSizeMB
) => {
  console.log(
    createResultsLogMessage(resultsFileGlobs, coverageFileGlobs, serverUrl)
  );

  const resultsBlob = collectResults(resultsFileGlobs);
  const performanceResults = collectPerformanceResults(performanceFileGlobs);
  const coverageFilePayloads = collectCoverage(
    coverageFileGlobs,
    baseDirectoryPath
  );

  if (resultsBlob.length > 0 || performanceResults.length > 0) {
    try {
      const resultsResponseData = await sendResults(
        serverUrl,
        publishToken,
        resultsBlob,
        performanceResults,
        coverageFilePayloads,
        gitRepoName,
        gitBranchName,
        gitCommitSha,
        gitPullRequestNumber,
        projectName,
        isCI,
        group,
        compressionEnabled,
        resultsMaxSizeMB
      );

      const publicId = resultsResponseData.id;
      const reportUrl = `${serverUrl}${resultsResponseData.uri}`;
      console.log(`View Projektor results at ${reportUrl}`);

      await collectAndSendAttachments(
        serverUrl,
        publishToken,
        attachmentFileGlobs,
        publicId,
        attachmentMaxSizeMB
      );

      return { resultsBlob, publicId, reportUrl, performanceResults };
    } catch (e) {
      console.error(
        `Error publishing results to Projektor server ${serverUrl}`,
        e.message
      );
      if (e.response && e.response.data) {
        console.error("Error from server", e.response.data.error_message);
      }

      return {
        resultsBlob,
        performanceResults,
        publicId: null,
        reportUrl: null,
      };
    }
  } else {
    return { resultsBlob, performanceResults, publicId: null, reportUrl: null };
  }
};

module.exports = {
  collectResults,
  sendResults,
  collectAndSendResults,
};
