const {
  writeSlackMessageFileToDisk,
  createSlackMessageJson,
  writeNoResultsSlackMessageFileToDisk,
  createNoResultsSlackMessageJson,
} = require("../slack");
const fs = require("fs");

describe("slack messages", () => {
  const slackMessageFileName = "projektor_failure_message.json";

  afterEach(() => {
    if (fs.existsSync(slackMessageFileName)) {
      fs.unlinkSync(slackMessageFileName);
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
      currentTimestamp,
    );

    expect(messageJson).toEqual(
      '{"attachments":[{"fallback":"Projektor test report","color":"#FF0000","pretext":"Tests failed in project my-project","title":"Projektor test report","title_link":"http://localhost/tests/ABC12345","text":"See the Projektor test report for details on the failing tests","footer":"Projektor","ts":1596674144043}]}',
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
      currentTimestamp,
    );

    expect(messageJson).toEqual(
      '{"attachments":[{"fallback":"Projektor test report","color":"#00FF00","pretext":"Tests passed in project my-project","title":"Projektor test report","title_link":"http://localhost/tests/ABC12345","text":"See the Projektor test report for details on the tests","footer":"Projektor","ts":1596674144043}]}',
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
      currentTimestamp,
    );

    expect(messageJson).toEqual(
      '{"attachments":[{"fallback":"Projektor test report","color":"#00FF00","pretext":"Tests passed in project ","title":"Projektor test report","title_link":"http://localhost/tests/ABC12345","text":"See the Projektor test report for details on the tests","footer":"Projektor","ts":1596674144043}]}',
    );
  });

  it("should write slack message file to disk", () => {
    const reportUrl = "http://localhost/tests/DEF12345";
    const projectName = "my-disk-project";
    const testsFailed = true;

    writeSlackMessageFileToDisk(
      reportUrl,
      slackMessageFileName,
      projectName,
      testsFailed,
    );

    expect(fs.existsSync(slackMessageFileName)).toBeTruthy();
  });

  it("should create no-results message", () => {
    const projectName = "my-project";
    const currentTimestamp = 1596674144044;

    const messageJson = createNoResultsSlackMessageJson(
      projectName,
      currentTimestamp,
    );

    expect(messageJson).toEqual(
      '{"attachments":[{"fallback":"Projektor test report","color":"#FFFF00","pretext":"No test results found","title":"Projektor test report","text":"No test results found to publish in project my-project","footer":"Projektor","ts":1596674144044}]}',
    );
  });

  it("should write no-results file to disk", () => {
    writeNoResultsSlackMessageFileToDisk(slackMessageFileName, "no results");

    expect(fs.existsSync(slackMessageFileName)).toBeTruthy();
  });
});
