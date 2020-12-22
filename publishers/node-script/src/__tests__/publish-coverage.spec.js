const axios = require("axios");
const MockAdapter = require("axios-mock-adapter");
const { collectAndSendResults } = require("../publish");

describe("publish with coverage", () => {
  let mockAxios;

  beforeEach(() => {
    mockAxios = new MockAdapter(axios);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should publish results with coverage to server all with token", async () => {
    const fileGlob = "src/__tests__/resultsDir1/*.xml";
    const coverageGlob = "src/__tests__/coverageDir1/*.xml";
    const serverUrl = "http://localhost:8080";

    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    const publishToken = "myPublishToken";

    await collectAndSendResults(serverUrl, publishToken, [fileGlob], null, [
      coverageGlob,
    ]);

    expect(mockAxios.history.post.length).toBe(1);

    const resultsPostRequest = mockAxios.history.post.find((postRequest) =>
      postRequest.url.includes("groupedResults")
    );
    expect(resultsPostRequest.headers["X-PROJEKTOR-TOKEN"]).toBe(publishToken);

    const resultsPostData = resultsPostRequest.data;
    expect(resultsPostData).toContain("resultsDir1-results1");
    expect(resultsPostData).toContain("resultsDir1-results2");

    const resultsPostBody = JSON.parse(resultsPostData);
    expect(resultsPostBody.coverageFiles.length).toBe(1);
    expect(resultsPostBody.coverageFiles[0].reportContents).toContain(
      "<coverage"
    );
  });

  it("should include base directory path when publishing coverage", async () => {
    const fileGlob = "src/__tests__/resultsDir1/*.xml";
    const coverageGlob = "src/__tests__/coverageDir1/*.xml";
    const serverUrl = "http://localhost:8080";

    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    mockAxios
      .onPost("http://localhost:8080/run/ABC123/coverageFile")
      .reply(200);

    const publishToken = "myPublishToken";

    await collectAndSendResults(
      serverUrl,
      publishToken,
      [fileGlob],
      null,
      [coverageGlob],
      null,
      null,
      null,
      null,
      null,
      null,
      false,
      "base/dir"
    );

    const resultsPostRequest = mockAxios.history.post.find((postRequest) =>
      postRequest.url.includes("groupedResults")
    );
    const resultsPostData = resultsPostRequest.data;
    const resultsPostBody = JSON.parse(resultsPostData);

    expect(resultsPostBody.coverageFiles.length).toBe(1);
    expect(resultsPostBody.coverageFiles[0].baseDirectoryPath).toEqual(
      "base/dir"
    );
  });
});
