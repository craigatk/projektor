const { run } = require("../index");
const MockAdapter = require("axios-mock-adapter");
const axios = require("axios");

describe("node script index - performance results", () => {
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

  it("should send performance results", async () => {
    const serverUrl = "http://localhost:8080";
    const performance = ["src/__tests__/performanceResults/*.json"];
    const compressionEnabled = false;

    await run(
      { performance, serverUrl, compressionEnabled },
      {},
      null,
      "projektor.none.json"
    );

    expect(mockAxios.history.post.length).toBe(1);

    const resultsPostRequest = mockAxios.history.post.find((postRequest) =>
      postRequest.url.includes("groupedResults")
    );

    const parsedRequestBody = JSON.parse(resultsPostRequest.data.toString());
    expect(parsedRequestBody.groupedTestSuites.length).toBe(0);

    expect(parsedRequestBody.performanceResults.length).toBe(2);
    const file1 = parsedRequestBody.performanceResults.find(
      (file) => file.name === "perf-test-1.json"
    );
    expect(file1.resultsBlob).toBe('{"name":"perf-test-1"}');

    const file2 = parsedRequestBody.performanceResults.find(
      (file) => file.name === "perf-test-2.json"
    );
    expect(file2.resultsBlob).toBe('{"name":"perf-test-2"}');
  });

  it("should log message when no performance results found", async () => {
    const serverUrl = "http://localhost:8080";
    const performance = ["does_not_exist/*.json"];
    const compressionEnabled = false;

    await run(
      { performance, serverUrl, compressionEnabled },
      {},
      null,
      "projektor.none.json"
    );

    expect(mockAxios.history.post.length).toBe(0);

    expect(consoleLog).toHaveBeenCalledWith(
      "No performance results files found in locations does_not_exist/*.json"
    );
  });
});
