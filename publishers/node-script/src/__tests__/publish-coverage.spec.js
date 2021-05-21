const axios = require("axios");
const MockAdapter = require("axios-mock-adapter");
const { collectAndSendResults } = require("../publish");

describe("publish with coverage", () => {
  let mockAxios;
  let consoleLog;

  beforeEach(() => {
    mockAxios = new MockAdapter(axios);
    consoleLog = jest.spyOn(console, "log").mockImplementation();
  });

  afterEach(() => {
    mockAxios.restore();
    consoleLog.mockRestore();
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

  it("should log message with coverage file globs", async () => {
    const fileGlob = "src/__tests__/resultsDir1/*.xml";
    const coverageGlobs = [
      "src/__tests__/coverageDir1/*.xml",
      "src/__tests__/coverageDir2/*.xml",
    ];
    const serverUrl = "http://localhost:8080";

    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    await collectAndSendResults(
      serverUrl,
      null,
      [fileGlob],
      null,
      coverageGlobs,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      false,
      null
    );

    const resultsPostRequest = mockAxios.history.post.find((postRequest) =>
      postRequest.url.includes("groupedResults")
    );
    const resultsPostData = resultsPostRequest.data;
    const resultsPostBody = JSON.parse(resultsPostData);

    expect(resultsPostBody.coverageFiles.length).toBe(2);

    expect(consoleLog).toHaveBeenCalledWith(
      "Gathering results from src/__tests__/resultsDir1/*.xml and coverage from src/__tests__/coverageDir1/*.xml,src/__tests__/coverageDir2/*.xml to send to Projektor server http://localhost:8080"
    );
  });
});
