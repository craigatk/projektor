const axios = require("axios");
const MockAdapter = require("axios-mock-adapter");
const { collectAndSendResults } = require("../publish");

describe("Node script publishing with Git metadata", () => {
  let mockAxios;

  beforeEach(() => {
    mockAxios = new MockAdapter(axios);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should publish results along with Git repo, main branch, and project name", async () => {
    const fileGlob = "src/__tests__/resultsDir1/*.xml";
    const serverUrl = "http://localhost:8080";
    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    await collectAndSendResults(
      serverUrl,
      null,
      [fileGlob],
      null,
      null,
      null,
      null,
      "projektor/projektor",
      "main",
      "02fea3d66fab1de935e1ac3adb008a0abb9a61f3",
      22,
      "my-proj",
      true,
      null,
      false,
      null,
      40,
      40,
      ["main", "master"]
    );

    expect(mockAxios.history.post.length).toBe(1);

    const resultsPostRequest = mockAxios.history.post.find((postRequest) =>
      postRequest.url.includes("groupedResults")
    );

    const parsedRequestBody = JSON.parse(resultsPostRequest.data);

    expect(parsedRequestBody.groupedTestSuites.length).toBe(1);
    expect(parsedRequestBody.groupedTestSuites[0].testSuitesBlob).toContain(
      "resultsDir1-results1"
    );
    expect(parsedRequestBody.groupedTestSuites[0].testSuitesBlob).toContain(
      "resultsDir1-results2"
    );

    expect(parsedRequestBody.metadata.git.repoName).toEqual(
      "projektor/projektor"
    );
    expect(parsedRequestBody.metadata.git.branchName).toEqual("main");
    expect(parsedRequestBody.metadata.git.isMainBranch).toEqual(true);
    expect(parsedRequestBody.metadata.git.commitSha).toEqual(
      "02fea3d66fab1de935e1ac3adb008a0abb9a61f3"
    );
    expect(parsedRequestBody.metadata.git.projectName).toEqual("my-proj");
  });

  it("should publish results along with Git repo, non-mainline branch, and project name", async () => {
    const fileGlob = "src/__tests__/resultsDir1/*.xml";
    const serverUrl = "http://localhost:8080";
    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    await collectAndSendResults(
      serverUrl,
      null,
      [fileGlob],
      null,
      null,
      null,
      null,
      "projektor/projektor",
      "feature/branch",
      "02fea3d66fab1de935e1ac3adb008a0abb9a61f3",
      23,
      "my-proj",
      true,
      null,
      false,
      null,
      40,
      40,
      ["main", "master"]
    );

    expect(mockAxios.history.post.length).toBe(1);

    const resultsPostRequest = mockAxios.history.post.find((postRequest) =>
      postRequest.url.includes("groupedResults")
    );

    const parsedRequestBody = JSON.parse(resultsPostRequest.data);

    expect(parsedRequestBody.metadata.git.repoName).toEqual(
      "projektor/projektor"
    );
    expect(parsedRequestBody.metadata.git.branchName).toEqual("feature/branch");
    expect(parsedRequestBody.metadata.git.isMainBranch).toEqual(false);
    expect(parsedRequestBody.metadata.git.commitSha).toEqual(
      "02fea3d66fab1de935e1ac3adb008a0abb9a61f3"
    );
    expect(parsedRequestBody.metadata.git.pullRequestNumber).toEqual(23);
    expect(parsedRequestBody.metadata.git.projectName).toEqual("my-proj");
  });

  it("should publish results along with non-main branch specified as mainline branch ", async () => {
    const fileGlob = "src/__tests__/resultsDir1/*.xml";
    const serverUrl = "http://localhost:8080";
    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    await collectAndSendResults(
      serverUrl,
      null,
      [fileGlob],
      null,
      null,
      null,
      null,
      "projektor/projektor",
      "develop",
      "02fea3d66fab1de935e1ac3adb008a0abb9a61f3",
      23,
      "my-proj",
      true,
      null,
      false,
      null,
      40,
      40,
      ["main", "develop"]
    );

    expect(mockAxios.history.post.length).toBe(1);

    const resultsPostRequest = mockAxios.history.post.find((postRequest) =>
      postRequest.url.includes("groupedResults")
    );

    const parsedRequestBody = JSON.parse(resultsPostRequest.data);

    expect(parsedRequestBody.metadata.git.branchName).toEqual("develop");
    expect(parsedRequestBody.metadata.git.isMainBranch).toEqual(true);
  });
});
