const axios = require("axios");
const fs = require("fs");
const path = require("path");
const { gzip } = require("node-gzip");
const { globsToFilePaths } = require("./file-utils");
const { collectAndSendCoverage } = require("./publish-coverage");
const { collectAndSendAttachments } = require("./publish-attachments");

const collectResults = (resultsFileGlobs) => {
  let resultsBlob = "";

  const resultsFilePaths = globsToFilePaths(resultsFileGlobs);

  if (resultsFilePaths.length > 0) {
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
  gitRepoName,
  gitBranchName,
  projectName,
  isCI,
  compressionEnabled
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
    metadata: {
      git: {
        repoName: gitRepoName,
        branchName: gitBranchName,
        isMainBranch: gitBranchName === "main" || gitBranchName === "master",
        projectName,
      },
      ci: isCI,
    },
  };

  const compressionConfig = {
    headers: {
      "Content-Encoding": "gzip",
    },
  };

  const resp = await axiosInstance.post(
    `${serverUrl}/groupedResults`,
    compressionEnabled
      ? await gzip(JSON.stringify(groupedResults))
      : groupedResults,
    compressionEnabled ? compressionConfig : null
  );

  return resp.data;
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
  projectName,
  isCI,
  compressionEnabled,
  baseDirectoryPath
) => {
  console.log(
    `Gathering results from ${resultsFileGlobs} to send to Projektor server ${serverUrl}`
  );

  const resultsBlob = collectResults(resultsFileGlobs);
  const performanceResults = collectPerformanceResults(performanceFileGlobs);

  if (resultsBlob.length > 0 || performanceResults.length > 0) {
    try {
      const resultsResponseData = await sendResults(
        serverUrl,
        publishToken,
        resultsBlob,
        performanceResults,
        gitRepoName,
        gitBranchName,
        projectName,
        isCI,
        compressionEnabled
      );

      const publicId = resultsResponseData.id;
      const reportUrl = `${serverUrl}${resultsResponseData.uri}`;
      console.log(`View Projektor results at ${reportUrl}`);

      await collectAndSendAttachments(
        serverUrl,
        publishToken,
        attachmentFileGlobs,
        publicId
      );

      await collectAndSendCoverage(
        serverUrl,
        publishToken,
        coverageFileGlobs,
        publicId,
        baseDirectoryPath,
        compressionEnabled
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
