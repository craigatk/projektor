const MockAdapter = require("axios-mock-adapter");
const axios = require("axios");
const { run } = require("../index");

describe("node script index - coverage reports", () => {
  let mockAxios;

  beforeEach(() => {
    mockAxios = new MockAdapter(axios);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should be able to publish code coverage reports by themselves", async () => {
    const serverUrl = "http://localhost:8080";
    const coverage = "src/__tests__/coverageDir1/*.xml";

    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    await run(
      { coverage, serverUrl, compressionEnabled: false },
      {},
      null,
      "projektor.none.json"
    );

    expect(mockAxios.history.post.length).toBe(1);

    const resultsPostRequest = mockAxios.history.post.find((postRequest) =>
      postRequest.url.includes("groupedResults")
    );

    const resultsPostData = resultsPostRequest.data;

    const resultsPostBody = JSON.parse(resultsPostData);
    expect(resultsPostBody.coverageFiles.length).toBe(1);
    expect(resultsPostBody.coverageFiles[0].reportContents).toContain(
      "<coverage"
    );
  });
});
