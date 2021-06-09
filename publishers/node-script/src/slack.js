function writeSlackMessageFileToDisk(
  reportUrl,
  slackMessageFileName,
  projectName,
  testsFailed
) {
  const fs = require("fs");

  const messageJson = createSlackMessageJson(
    reportUrl,
    projectName,
    testsFailed
  );

  fs.writeFileSync(slackMessageFileName, messageJson);
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

module.exports = {
  writeSlackMessageFileToDisk,
  createSlackMessageJson,
};
