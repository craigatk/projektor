const axios = require("axios");
const MockAdapter = require("axios-mock-adapter");
const { collectAndSendResults } = require("../publish");

describe("Node script publishing with performance results", () => {
  let mockAxios;

  beforeEach(() => {
    mockAxios = new MockAdapter(axios);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should publish performance results only from directory", async () => {
    const performanceFileGlob = "src/__tests__/performanceResults/*.json";
    const serverUrl = "http://localhost:8080";
    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    await collectAndSendResults(
      serverUrl,
      null,
      null,
      null,
      null,
      [performanceFileGlob],
      "projektor/projektor",
      "main",
      "my-proj",
      true
    );

    expect(mockAxios.history.post.length).toBe(1);

    const resultsPostRequest = mockAxios.history.post.find((postRequest) =>
      postRequest.url.includes("groupedResults")
    );

    const parsedRequestBody = JSON.parse(resultsPostRequest.data);
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

  it("should publish performance results only from one file", async () => {
    const performanceFileGlob =
      "src/__tests__/performanceResults/perf-test-1.json";
    const serverUrl = "http://localhost:8080";
    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    await collectAndSendResults(
      serverUrl,
      null,
      null,
      null,
      null,
      [performanceFileGlob],
      "projektor/projektor",
      "main",
      "my-proj",
      true
    );

    expect(mockAxios.history.post.length).toBe(1);

    const resultsPostRequest = mockAxios.history.post.find((postRequest) =>
      postRequest.url.includes("groupedResults")
    );

    const parsedRequestBody = JSON.parse(resultsPostRequest.data);
    expect(parsedRequestBody.groupedTestSuites.length).toBe(0);

    expect(parsedRequestBody.performanceResults.length).toBe(1);
    expect(parsedRequestBody.performanceResults[0].name).toBe(
      "perf-test-1.json"
    );
    expect(parsedRequestBody.performanceResults[0].resultsBlob).toBe(
      '{"name":"perf-test-1"}'
    );
  });
});
