const axios = require("axios");

const fetchTestRunSummary = (testRunId, serverPort) => {
  const url = `http://localhost:${serverPort}/run/${testRunId}/summary`;

  return axios.get(url);
};

const fetchAttachments = (testRunId, serverPort) =>
  axios.get(`http://localhost:${serverPort}/run/${testRunId}/attachments`);

const fetchAttachment = (attachmentName, testRunId, serverPort) => {
  const url = `http://localhost:${serverPort}/run/${testRunId}/attachments/${attachmentName}`;

  return axios.get(url);
};

const fetchCoverage = (testRunId, serverPort) =>
  axios.get(`http://localhost:${serverPort}/run/${testRunId}/coverage`);

const fetchCoverageFiles = (testRunId, coverageGroupName, serverPort) =>
  axios.get(
    `http://localhost:${serverPort}/run/${testRunId}/coverage/${coverageGroupName}/files`
  );

const fetchResultsMetadata = (testRunId, serverPort) =>
  axios.get(`http://localhost:${serverPort}/run/${testRunId}/metadata`);

const fetchGitMetadata = (testRunId, serverPort) =>
  axios.get(`http://localhost:${serverPort}/run/${testRunId}/metadata/git`);

const fetchPerformanceResults = (testRunId, serverPort) =>
  axios.get(`http://localhost:${serverPort}/run/${testRunId}/performance`);

const fetchCodeQuality = (testRunId, serverPort) =>
  axios.get(`http://localhost:${serverPort}/run/${testRunId}/quality`);

module.exports = {
  fetchTestRunSummary,
  fetchAttachments,
  fetchAttachment,
  fetchCoverage,
  fetchCoverageFiles,
  fetchResultsMetadata,
  fetchGitMetadata,
  fetchPerformanceResults,
  fetchCodeQuality,
};
