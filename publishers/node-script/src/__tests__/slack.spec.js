const {
  writeSlackMessageFileToDisk,
  createSlackMessageJson,
} = require("../slack");
const fs = require("fs");

describe("slack messages", () => {
  afterEach(() => {
    if (fs.existsSync("projektor_failure_message.json")) {
      fs.unlinkSync("projektor_failure_message.json");
    }
  });

  it("should create slack message", () => {
    const reportUrl = "http://localhost/tests/ABC12345";
    const projectName = "my-project";
    const currentTimestamp = 1596674144043;

    const messageJson = createSlackMessageJson(
      reportUrl,
      projectName,
      currentTimestamp
    );

    expect(messageJson).toEqual(
      '{"attachments":[{"fallback":"Projektor test report","color":"#FF0000","pretext":"Tests failed in project my-project","title":"Projektor test report","title_link":"http://localhost/tests/ABC12345","text":"See the Projektor test report for details on the failing tests","footer":"Projektor","ts":1596674144043}]}'
    );
  });

  it("should write slack message file to disk", () => {
    const reportUrl = "http://localhost/tests/DEF12345";
    const projectName = "my-disk-project";

    writeSlackMessageFileToDisk(
      reportUrl,
      "projektor_failure_message.json",
      projectName
    );

    expect(fs.existsSync("projektor_failure_message.json")).toBeTruthy();
  });
});
