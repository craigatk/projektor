const axios = require("axios");
const MockAdapter = require("axios-mock-adapter");
const { collectAndSendResults } = require("../publish");

describe("Node script publishing with results metadata", () => {
  let mockAxios;

  beforeEach(() => {
    mockAxios = new MockAdapter(axios);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should publish results along with metadata", async () => {
    const fileGlob = "src/__tests__/resultsDir1/*.xml";
    const serverUrl = "http://localhost:8080";
    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    const isCI = true;

    await collectAndSendResults(
      serverUrl,
      null,
      [fileGlob],
      null,
      null,
      "projektor/projektor",
      "main",
      "my-proj",
      isCI
    );

    expect(mockAxios.history.post.length).toBe(1);

    const resultsPostRequest = mockAxios.history.post.find((postRequest) =>
      postRequest.url.includes("groupedResults")
    );

    const parsedRequestBody = JSON.parse(resultsPostRequest.data);
    expect(parsedRequestBody.groupedTestSuites.length).toBe(1);

    expect(parsedRequestBody.metadata.ci).toBe(true);
  });
});
