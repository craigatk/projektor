function writeSlackMessageFileToDisk(
  reportUrl,
  slackMessageFileName,
  projectName
) {
  const fs = require("fs");

  const messageJson = createSlackMessageJson(reportUrl, projectName);

  fs.writeFileSync(slackMessageFileName, messageJson);
}

function createSlackMessageJson(reportUrl, projectName, currentTimestamp) {
  const message = {
    attachments: [
      {
        fallback: "Projektor test report",
        color: "#FF0000",
        pretext: `Tests failed in project ${projectName || ""}`,
        title: "Projektor test report",
        title_link: reportUrl,
        text: "See the Projektor test report for details on the failing tests",
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
