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

});
