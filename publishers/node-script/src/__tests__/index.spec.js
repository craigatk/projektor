const waitForExpect = require("wait-for-expect");
const axios = require("axios");
const MockAdapter = require("axios-mock-adapter");
const { run } = require("../index");

describe("node script index", () => {
  let mockAxios;
  let consoleError;

  beforeEach(() => {
    mockAxios = new MockAdapter(axios);
    consoleError = jest.spyOn(console, "error").mockImplementation();
    process.exitCode = 0;
  });

  afterEach(() => {
    mockAxios.restore();
    consoleError.mockRestore();
  });

  it("should use settings from projektor.json if there is one", async () => {
    mockAxios
      .onPost("http://localhost:8080/results")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    run([], null, "src/__tests__/projektor.test.json");

    await waitForExpect(() => {
      expect(mockAxios.history.post.length).toBe(1);

      const postRequest = mockAxios.history.post[0];
      const postData = postRequest.data;

      expect(postData).toContain("resultsDir1-results1");
      expect(postData).toContain("resultsDir1-results2");
    });

    expect(process.exitCode).not.toBe(1);
  });

  it("should use settings from custom projektor.json if it is specified on command line", async () => {
    mockAxios
      .onPost("http://localhost:8080/results")
      .reply(200, { id: "DEF345", uri: "/tests/DEF345" });

    run(
      ["--configFile=src/__tests__/projektor.test.json"],
      null,
      "projektor.fake.json"
    );

    await waitForExpect(() => {
      expect(mockAxios.history.post.length).toBe(1);

      const postRequest = mockAxios.history.post[0];
      const postData = postRequest.data;

      expect(postData).toContain("resultsDir1-results1");
      expect(postData).toContain("resultsDir1-results2");
    });

    expect(process.exitCode).not.toBe(1);
  });

  it("should log error when no results dirs specified", () => {
    run(
      ["--configFile=src/__tests__/projektor.missing.results.json"],
      null,
      "projektor.fake.json"
    );

    expect(consoleError).toHaveBeenLastCalledWith(
      expect.stringContaining("Results files not configured")
    );
  });

  it("should exit with non-zero exit code when a test failure and configured via CLI", async () => {
    mockAxios
      .onPost("http://localhost:8080/results")
      .reply(200, { id: "FOO123", uri: "/tests/FOO123" });

    run(
      [
        "--serverUrl=http://localhost:8080/results",
        "--exitWithFailure",
        "src/__tests__/resultsWithFailure/*.xml",
      ],
      null,
      "projektor.none.json"
    );

    expect(process.exitCode).toBe(1);

    await waitForExpect(() => {
      expect(mockAxios.history.post.length).toBe(1);

      const postRequest = mockAxios.history.post[0];
      const postData = postRequest.data;
      expect(postData).toContain("side nav");
    });
  });

  it("should not exit with non-zero exit code when all tests passed and configured via CLI", async () => {
    mockAxios
      .onPost("http://localhost:8080/results")
      .reply(200, { id: "FOO345", uri: "/tests/FOO345" });

    run(
      [
        "--serverUrl=http://localhost:8080/results",
        "--exitWithFailure",
        "src/__tests__/resultsDir1/*.xml",
      ],
      null,
      "projektor.none.json"
    );

    expect(process.exitCode).not.toBe(1);

    await waitForExpect(() => {
      expect(mockAxios.history.post.length).toBe(1);

      const postRequest = mockAxios.history.post[0];
      const postData = postRequest.data;
      expect(postData).toContain("resultsDir1-results1");
    });
  });
});
