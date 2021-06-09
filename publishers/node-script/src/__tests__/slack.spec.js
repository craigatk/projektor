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

  it("should create slack message when tests failed", () => {
    const reportUrl = "http://localhost/tests/ABC12345";
    const projectName = "my-project";
    const testsFailed = true;
    const currentTimestamp = 1596674144043;

    const messageJson = createSlackMessageJson(
      reportUrl,
      projectName,
      testsFailed,
      currentTimestamp
    );

    expect(messageJson).toEqual(
      '{"attachments":[{"fallback":"Projektor test report","color":"#FF0000","pretext":"Tests failed in project my-project","title":"Projektor test report","title_link":"http://localhost/tests/ABC12345","text":"See the Projektor test report for details on the failing tests","footer":"Projektor","ts":1596674144043}]}'
    );
  });

  it("should create slack message when tests passed with project name", () => {
    const reportUrl = "http://localhost/tests/ABC12345";
    const projectName = "my-project";
    const testsFailed = false;
    const currentTimestamp = 1596674144043;

    const messageJson = createSlackMessageJson(
      reportUrl,
      projectName,
      testsFailed,
      currentTimestamp
    );

    expect(messageJson).toEqual(
      '{"attachments":[{"fallback":"Projektor test report","color":"#00FF00","pretext":"Tests passed in project my-project","title":"Projektor test report","title_link":"http://localhost/tests/ABC12345","text":"See the Projektor test report for details on the tests","footer":"Projektor","ts":1596674144043}]}'
    );
  });

  it("should create slack message when tests passed without project name", () => {
    const reportUrl = "http://localhost/tests/ABC12345";
    const projectName = null;
    const testsFailed = false;
    const currentTimestamp = 1596674144043;

    const messageJson = createSlackMessageJson(
      reportUrl,
      projectName,
      testsFailed,
      currentTimestamp
    );

    expect(messageJson).toEqual(
      '{"attachments":[{"fallback":"Projektor test report","color":"#00FF00","pretext":"Tests passed in project ","title":"Projektor test report","title_link":"http://localhost/tests/ABC12345","text":"See the Projektor test report for details on the tests","footer":"Projektor","ts":1596674144043}]}'
    );
  });

  it("should write slack message file to disk", () => {
    const reportUrl = "http://localhost/tests/DEF12345";
    const projectName = "my-disk-project";
    const testsFailed = true;

    writeSlackMessageFileToDisk(
      reportUrl,
      "projektor_failure_message.json",
      projectName,
      testsFailed
    );

    expect(fs.existsSync("projektor_failure_message.json")).toBeTruthy();
  });
});
