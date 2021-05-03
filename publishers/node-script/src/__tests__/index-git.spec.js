const axios = require("axios");
const MockAdapter = require("axios-mock-adapter");
const {
  extractUncompressedResultsPostData,
} = require("./util/compression-util");
const { run } = require("../index");

describe("node script index - Git source control values", () => {
  let mockAxios;

  beforeEach(() => {
    mockAxios = new MockAdapter(axios);
    process.exitCode = 0;
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should use branch name from ref environment variable when there is one", async () => {
    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    await run(
      {
        serverUrl: "http://localhost:8080",
        resultsFileGlobs: ["src/__tests__/resultsDir1/*.xml"],
      },
      { VELA_BUILD_REF: "refs/heads/my-branch" },
      null,
      "src/__tests__/does_not_exist.json"
    );
    expect(mockAxios.history.post.length).toBe(1);

    const postRequest = mockAxios.history.post[0];
    expect(postRequest.url).toContain("/groupedResults");
    const postData = await extractUncompressedResultsPostData(mockAxios);
    const parsedGroupResults = JSON.parse(postData);

    expect(parsedGroupResults.metadata.git).toBeDefined();
    expect(parsedGroupResults.metadata.git.branchName).toBe("my-branch");
  });

  it("should use branch name from environment variable when there is one", async () => {
    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    await run(
      {
        serverUrl: "http://localhost:8080",
        resultsFileGlobs: ["src/__tests__/resultsDir1/*.xml"],
      },
      { VELA_PULL_REQUEST_SOURCE: "my-branch" },
      null,
      "src/__tests__/does_not_exist.json"
    );
    expect(mockAxios.history.post.length).toBe(1);

    const postRequest = mockAxios.history.post[0];
    expect(postRequest.url).toContain("/groupedResults");
    const postData = await extractUncompressedResultsPostData(mockAxios);
    const parsedGroupResults = JSON.parse(postData);

    expect(parsedGroupResults.metadata.git).toBeDefined();
    expect(parsedGroupResults.metadata.git.branchName).toBe("my-branch");
    expect(parsedGroupResults.metadata.git.pullRequestNumber).toBe(null);
  });

  it("should use repo and branch name from Drone environment variables when they are set", async () => {
    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    await run(
      {
        serverUrl: "http://localhost:8080",
        resultsFileGlobs: ["src/__tests__/resultsDir1/*.xml"],
      },
      {
        DRONE_REPO: "org/repo",
        DRONE_COMMIT_BRANCH: "main",
        DRONE_PULL_REQUEST: "42",
      },
      null,
      "src/__tests__/does_not_exist.json"
    );
    expect(mockAxios.history.post.length).toBe(1);

    const postRequest = mockAxios.history.post[0];
    expect(postRequest.url).toContain("/groupedResults");
    const postData = await extractUncompressedResultsPostData(mockAxios);
    const parsedGroupResults = JSON.parse(postData);

    expect(parsedGroupResults.metadata.git).toBeDefined();
    expect(parsedGroupResults.metadata.git.repoName).toBe("org/repo");
    expect(parsedGroupResults.metadata.git.branchName).toBe("main");
    expect(parsedGroupResults.metadata.git.isMainBranch).toBe(true);
    expect(parsedGroupResults.metadata.git.pullRequestNumber).toBe(42);
  });

  it("should use pull request number from environment variable when it is set", async () => {
    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    await run(
      {
        serverUrl: "http://localhost:8080",
        resultsFileGlobs: ["src/__tests__/resultsDir1/*.xml"],
      },
      { VELA_BUILD_PULL_REQUEST: "42" },
      null,
      "src/__tests__/does_not_exist.json"
    );
    expect(mockAxios.history.post.length).toBe(1);

    const postRequest = mockAxios.history.post[0];
    expect(postRequest.url).toContain("/groupedResults");
    const postData = await extractUncompressedResultsPostData(mockAxios);
    const parsedGroupResults = JSON.parse(postData);

    expect(parsedGroupResults.metadata.git).toBeDefined();
    expect(parsedGroupResults.metadata.git.pullRequestNumber).toBe(42);
  });

  it("should use build number from environment variable as group when group enabled", async () => {
    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    await run(
      {
        serverUrl: "http://localhost:8080",
        resultsFileGlobs: ["src/__tests__/resultsDir1/*.xml"],
        groupResults: true,
      },
      { VELA_BUILD_PULL_REQUEST: "42", VELA_BUILD_NUMBER: "22" },
      null,
      "src/__tests__/does_not_exist.json"
    );
    expect(mockAxios.history.post.length).toBe(1);

    const postRequest = mockAxios.history.post[0];
    expect(postRequest.url).toContain("/groupedResults");
    const postData = await extractUncompressedResultsPostData(mockAxios);
    const parsedGroupResults = JSON.parse(postData);

    expect(parsedGroupResults.metadata.group).toBe("22");
  });

  it("should not use build number from environment variable as group when not group enabled", async () => {
    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    await run(
      {
        serverUrl: "http://localhost:8080",
        resultsFileGlobs: ["src/__tests__/resultsDir1/*.xml"],
        groupResults: null,
      },
      { VELA_BUILD_PULL_REQUEST: "42", VELA_BUILD_NUMBER: "22" },
      null,
      "src/__tests__/does_not_exist.json"
    );
    expect(mockAxios.history.post.length).toBe(1);

    const postRequest = mockAxios.history.post[0];
    expect(postRequest.url).toContain("/groupedResults");
    const postData = await extractUncompressedResultsPostData(mockAxios);
    const parsedGroupResults = JSON.parse(postData);

    expect(parsedGroupResults.metadata.group).toBe(null);
  });
});
