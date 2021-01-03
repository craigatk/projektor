const axios = require("axios");
const MockAdapter = require("axios-mock-adapter");
const {
  extractUncompressedResultsPostData,
} = require("./util/compression-util");
const {
  collectResults,
  sendResults,
  collectAndSendResults,
} = require("../publish");

describe("Projektor publisher", () => {
  let mockAxios;

  beforeEach(() => {
    mockAxios = new MockAdapter(axios);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should handle error when posting to server", async () => {
    const fileGlob = "src/__tests__/resultsDir1/*.xml";
    const serverUrl = "http://localhost:8080";

    mockAxios.onPost("http://localhost:8080/results").reply(400, null);

    await collectAndSendResults(serverUrl, null, [fileGlob]);

    expect(mockAxios.history.post.length).toBe(1);

    const postData = mockAxios.history.post[0].data;

    expect(postData).toContain("resultsDir1-results1");
    expect(postData).toContain("resultsDir1-results2");
  });

});
