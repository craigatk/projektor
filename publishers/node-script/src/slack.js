const fs = require("fs");

function writeFileWithData(
  fileName,
  data
) {
  console.log("Writing Slack message data to " + fileName);
  fs.writeFileSync(fileName, data);
}

function writeSlackMessageFileToDisk(
  reportUrl,
  slackMessageFileName,
  projectName,
  testsFailed
) {
  const messageJson = createSlackMessageJson(
    reportUrl,
    projectName,
    testsFailed
  );

  writeFileWithData(slackMessageFileName, messageJson);
}

function createSlackMessageJson(
  reportUrl,
  projectName,
  testsFailed,
  currentTimestamp
) {
  const projectNameToDisplay = projectName || "";

  const message = {
    attachments: [
      {
        fallback: "Projektor test report",
        color: testsFailed ? "#FF0000" : "#00FF00",
        pretext: testsFailed
          ? `Tests failed in project ${projectNameToDisplay}`
          : `Tests passed in project ${projectNameToDisplay}`,
        title: "Projektor test report",
        title_link: reportUrl,
        text: testsFailed
          ? "See the Projektor test report for details on the failing tests"
          : "See the Projektor test report for details on the tests",
        footer: "Projektor",
        ts: currentTimestamp || new Date().getTime(),
      },
    ],
  };

  return JSON.stringify(message);
}

function writeNoResultsSlackMessageFileToDisk(
  slackMessageFileName,
  projectName
) {
  const messageJson = createNoResultsSlackMessageJson(projectName);

  writeFileWithData(slackMessageFileName, messageJson);
}

function createNoResultsSlackMessageJson(projectName, currentTimestamp) {
  const projectNameToDisplay = projectName || "";

  const message = {
    attachments: [
      {
        fallback: "Projektor test report",
        color: "#FFFF00",
        pretext: "No test results found",
        title: "Projektor test report",
        text: `No test results found to publish in project ${projectNameToDisplay}`,
        footer: "Projektor",
        ts: currentTimestamp || new Date().getTime(),
      },
    ],
  };

  return JSON.stringify(message);
}

module.exports = {
  writeSlackMessageFileToDisk,
  createSlackMessageJson,
  writeNoResultsSlackMessageFileToDisk,
  createNoResultsSlackMessageJson,
};
