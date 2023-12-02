const axios = require("axios");
const MockAdapter = require("axios-mock-adapter");
const {
  extractUncompressedResultsPostData,
} = require("./util/compression-util");
const { run, runCLI } = require("../index");

describe("node script index exit with failure", () => {
  let mockAxios;
  let consoleError;
  let consoleLog;

  beforeEach(() => {
    mockAxios = new MockAdapter(axios);
    consoleError = jest.spyOn(console, "error").mockImplementation();
    consoleLog = jest.spyOn(console, "log").mockImplementation();
    process.exitCode = 0;
  });

  afterEach(() => {
    mockAxios.restore();
    consoleError.mockRestore();
    consoleLog.mockRestore();
  });

  it("should exit with non-zero exit code when a test failure and configured via CLI", async () => {
    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "FOO123", uri: "/tests/FOO123" });

    await runCLI(
      [
        "--serverUrl=http://localhost:8080",
        "--exitWithFailure",
        "src/__tests__/resultsWithFailure/*.xml",
      ],
      null,
      "projektor.none.json",
    );

    expect(process.exitCode).toBe(1);

    const postData = await extractUncompressedResultsPostData(mockAxios);
    expect(postData).toContain("side nav");
  });

  it("should not exit with non-zero exit code when all tests passed and configured via CLI", async () => {
    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "FOO345", uri: "/tests/FOO345" });

    await runCLI(
      [
        "--serverUrl=http://localhost:8080",
        "--exitWithFailure",
        "src/__tests__/resultsDir1/*.xml",
      ],
      null,
      "projektor.none.json",
    );

    expect(process.exitCode).not.toBe(1);

    const postData = await extractUncompressedResultsPostData(mockAxios);
    expect(postData).toContain("resultsDir1-results1");
  });

  it("should exit with failure when config param set and publishing results fails with 400 from server", async () => {
    const resultsFileGlobs = ["src/__tests__/resultsDir1/*.xml"];
    const serverUrl = "http://localhost:8080";

    mockAxios.onPost("http://localhost:8080/groupedResults").reply(400, {});

    await run(
      { resultsFileGlobs, failOnPublishError: true, serverUrl },
      {},
      null,
      "projektor.none.json",
    );

    expect(process.exitCode).toBe(1);
  });

  it("should not exit with failure when config param not set and publishing results fails with 400 from server", async () => {
    const resultsFileGlobs = ["src/__tests__/resultsDir1/*.xml"];
    const serverUrl = "http://localhost:8080";

    mockAxios.onPost("http://localhost:8080/groupedResults").reply(400, {});

    await run({ resultsFileGlobs, serverUrl }, {}, null, "projektor.none.json");

    expect(process.exitCode).not.toBe(1);
  });
});
