const waitForExpect = require("wait-for-expect");
const axios = require("axios");
const MockAdapter = require("axios-mock-adapter");
const { run } = require("../index");

describe("node script index", () => {
  let mockAxios;

  beforeEach(() => {
    mockAxios = new MockAdapter(axios);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should use settings from projektor.json if there is one", async () => {
    mockAxios
      .onPost("http://localhost:8080/results")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    run([], "src/__tests__/projektor.test.json");

    await waitForExpect(() => {
      expect(mockAxios.history.post.length).toBe(1);

      const postData = mockAxios.history.post[0].data;

      expect(postData).toContain("resultsDir1-results1");
      expect(postData).toContain("resultsDir1-results2");
    });
  });
});
