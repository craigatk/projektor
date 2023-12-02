const axios = require("axios");
const MockAdapter = require("axios-mock-adapter");
const {
  extractUncompressedResultsPostData,
} = require("./util/compression-util");
const { run } = require("../index");

describe("node script index - code quality reports", () => {
  let mockAxios;

  beforeEach(() => {
    mockAxios = new MockAdapter(axios);
    process.exitCode = 0;
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should publish code quality reports when one directory passed as arg", async () => {
    const resultsFileGlobs = ["src/__tests__/resultsDir1/*.xml"];
    const codeQuality = "src/__tests__/codeQualityReports1/*.txt";
    const serverUrl = "http://localhost:8080";

    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    await run(
      { resultsFileGlobs, codeQuality, serverUrl },
      {},
      null,
      "projektor.none.json",
    );

    expect(mockAxios.history.post.length).toBe(1);
    const postRequest = mockAxios.history.post[0];
    expect(postRequest.url).toContain("/groupedResults");
    const postData = await extractUncompressedResultsPostData(mockAxios);

    expect(postData).toContain("Code quality 1");
    expect(postData).toContain("Code quality 2");

    expect(postData).toContain("codeQuality1.txt");
    expect(postData).toContain("codeQuality2.txt");

    expect(postData).toContain("resultsDir1-results1");
    expect(postData).toContain("resultsDir1-results2");

    expect(process.exitCode).not.toBe(1);
  });

  it("should publish code quality reports when two directories passed as arg", async () => {
    const resultsFileGlobs = ["src/__tests__/resultsDir1/*.xml"];
    const codeQuality = [
      "src/__tests__/codeQualityReports1/*.txt",
      "src/__tests__/codeQualityReports2/*.txt",
    ];
    const serverUrl = "http://localhost:8080";

    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    await run(
      { resultsFileGlobs, codeQuality, serverUrl },
      {},
      null,
      "projektor.none.json",
    );

    expect(mockAxios.history.post.length).toBe(1);
    const postRequest = mockAxios.history.post[0];
    expect(postRequest.url).toContain("/groupedResults");
    const postData = await extractUncompressedResultsPostData(mockAxios);

    expect(postData).toContain("Code quality 1");
    expect(postData).toContain("Code quality 2");
    expect(postData).toContain("Code quality 3");
    expect(postData).toContain("Code quality 4");

    expect(postData).toContain("codeQuality1.txt");
    expect(postData).toContain("codeQuality2.txt");
    expect(postData).toContain("codeQuality3.txt");
    expect(postData).toContain("codeQuality4.txt");

    expect(postData).toContain("resultsDir1-results1");
    expect(postData).toContain("resultsDir1-results2");

    expect(process.exitCode).not.toBe(1);
  });

  it("should publish when there are only code quality reports and no test results", async () => {
    const resultsFileGlobs = ["src/does-not-exist/*.xml"];
    const codeQuality = [
      "src/__tests__/codeQualityReports1/*.txt",
      "src/__tests__/codeQualityReports2/*.txt",
    ];
    const serverUrl = "http://localhost:8080";

    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    await run(
      { resultsFileGlobs, codeQuality, serverUrl },
      {},
      null,
      "projektor.none.json",
    );

    expect(mockAxios.history.post.length).toBe(1);
    const postRequest = mockAxios.history.post[0];
    expect(postRequest.url).toContain("/groupedResults");
    const postData = await extractUncompressedResultsPostData(mockAxios);

    expect(postData).toContain("Code quality 1");
    expect(postData).toContain("Code quality 2");
    expect(postData).toContain("Code quality 3");
    expect(postData).toContain("Code quality 4");

    expect(postData).toContain("codeQuality1.txt");
    expect(postData).toContain("codeQuality2.txt");
    expect(postData).toContain("codeQuality3.txt");
    expect(postData).toContain("codeQuality4.txt");

    expect(process.exitCode).not.toBe(1);
  });

  it("should publish code quality reports configured in config file", async () => {
    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    await run({}, {}, null, "src/__tests__/projektor.code-quality.json");

    expect(mockAxios.history.post.length).toBe(1);
    const postRequest = mockAxios.history.post[0];
    expect(postRequest.url).toContain("/groupedResults");
    const postData = await extractUncompressedResultsPostData(mockAxios);

    expect(postData).toContain("Code quality 1");
    expect(postData).toContain("Code quality 2");
    expect(postData).toContain("Code quality 3");
    expect(postData).toContain("Code quality 4");

    expect(postData).toContain("codeQuality1.txt");
    expect(postData).toContain("codeQuality2.txt");
    expect(postData).toContain("codeQuality3.txt");
    expect(postData).toContain("codeQuality4.txt");

    expect(postData).toContain("resultsDir1-results1");
    expect(postData).toContain("resultsDir1-results2");

    expect(process.exitCode).not.toBe(1);
  });
});
