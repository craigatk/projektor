const axios = require("axios");
const MockAdapter = require("axios-mock-adapter");
const { run } = require("../index");
const fs = require("fs");

describe("index Slack message file", () => {
  let mockAxios;

  const slackMessageFileName = "projektor_failure_message.json";

  beforeEach(() => {
    mockAxios = new MockAdapter(axios);
  });

  afterEach(() => {
    mockAxios.restore();

    if (fs.existsSync(slackMessageFileName)) {
      fs.unlinkSync(slackMessageFileName);
    }
  });

  it("should write Slack message file with general project name", async () => {
    const resultsFileGlobs = ["src/__tests__/resultsDir1/*.xml"];
    const serverUrl = "http://localhost:8080";
    const writeSlackMessageFile = true;
    const projectName = "my-project";

    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    await run(
      { resultsFileGlobs, serverUrl, projectName, writeSlackMessageFile },
      {},
      null,
      "projektor.none.json",
    );

    expect(fs.existsSync(slackMessageFileName)).toBeTruthy();

    const messageFileContents = fs
      .readFileSync(slackMessageFileName)
      .toString();
    const parsedMessage = JSON.parse(messageFileContents);
    expect(parsedMessage.attachments[0].pretext).toBe(
      `Tests passed in project ${projectName}`,
    );
  });

  it("should write Slack message file with specific Slack project name overriding general project name", async () => {
    const resultsFileGlobs = ["src/__tests__/resultsDir1/*.xml"];
    const serverUrl = "http://localhost:8080";
    const writeSlackMessageFile = true;
    const projectName = "my-project";
    const slackProjectName = "my-slack-project";

    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    await run(
      {
        resultsFileGlobs,
        serverUrl,
        projectName,
        slackProjectName,
        writeSlackMessageFile,
      },
      {},
      null,
      "projektor.none.json",
    );

    expect(fs.existsSync(slackMessageFileName)).toBeTruthy();

    const messageFileContents = fs
      .readFileSync(slackMessageFileName)
      .toString();
    const parsedMessage = JSON.parse(messageFileContents);
    expect(parsedMessage.attachments[0].pretext).toBe(
      `Tests passed in project ${slackProjectName}`,
    );
  });

  it("should write Slack message file with specific Slack project name only", async () => {
    const resultsFileGlobs = ["src/__tests__/resultsDir1/*.xml"];
    const serverUrl = "http://localhost:8080";
    const writeSlackMessageFile = true;
    const slackProjectName = "my-slack-project";

    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    await run(
      {
        resultsFileGlobs,
        serverUrl,
        slackProjectName,
        writeSlackMessageFile,
      },
      {},
      null,
      "projektor.none.json",
    );

    expect(fs.existsSync(slackMessageFileName)).toBeTruthy();

    const messageFileContents = fs
      .readFileSync(slackMessageFileName)
      .toString();
    const parsedMessage = JSON.parse(messageFileContents);
    expect(parsedMessage.attachments[0].pretext).toBe(
      `Tests passed in project ${slackProjectName}`,
    );
  });

  it("should write no-results Slack message file when there were no results", async () => {
    const resultsFileGlobs = ["src/doesNotExist/*.xml"];
    const serverUrl = "http://localhost:8080";
    const writeSlackMessageFile = true;
    const slackProjectName = "my-slack-project";

    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    await run(
      {
        resultsFileGlobs,
        serverUrl,
        slackProjectName,
        writeSlackMessageFile,
      },
      {},
      null,
      "projektor.none.json",
    );

    expect(fs.existsSync(slackMessageFileName)).toBeTruthy();

    const messageFileContents = fs
      .readFileSync(slackMessageFileName)
      .toString();
    const parsedMessage = JSON.parse(messageFileContents);
    const message = parsedMessage.attachments[0];
    expect(message.pretext).toBe("No test results found");
    expect(message.text).toBe(
      `No test results found to publish in project ${slackProjectName}`,
    );
  });
});
