const axios = require("axios");
const MockAdapter = require("axios-mock-adapter");
const { collectAndSendResults } = require("../publish");

describe("publish with attachments", () => {
  let mockAxios;

  beforeEach(() => {
    mockAxios = new MockAdapter(axios);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should publish results with attachments to server all with token", async () => {
    const fileGlob = "src/__tests__/resultsDir1/*.xml";
    const attachmentGlob = "src/__tests__/attachmentsDir1/*.txt";
    const serverUrl = "http://localhost:8080";

    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    mockAxios
      .onPost("http://localhost:8080/run/ABC123/attachments/attachment1.txt")
      .reply(200);

    mockAxios
      .onPost("http://localhost:8080/run/ABC123/attachments/attachment2.txt")
      .reply(200);

    const publishToken = "myPublishToken";

    await collectAndSendResults(
      serverUrl,
      publishToken,
      [fileGlob],
      [attachmentGlob],
    );

    expect(mockAxios.history.post.length).toBe(3);

    const resultsPostRequest = mockAxios.history.post.find((postRequest) =>
      postRequest.url.includes("groupedResults"),
    );
    expect(resultsPostRequest.headers["X-PROJEKTOR-TOKEN"]).toBe(publishToken);

    const postData = resultsPostRequest.data;
    expect(postData).toContain("resultsDir1-results1");
    expect(postData).toContain("resultsDir1-results2");

    const attachment1PostRequest = mockAxios.history.post.find((postRequest) =>
      postRequest.url.includes("attachments/attachment1.txt"),
    );
    expect(attachment1PostRequest.headers["X-PROJEKTOR-TOKEN"]).toBe(
      publishToken,
    );

    const attachment2PostRequest = mockAxios.history.post.find((postRequest) =>
      postRequest.url.includes("attachments/attachment2.txt"),
    );
    expect(attachment2PostRequest.headers["X-PROJEKTOR-TOKEN"]).toBe(
      publishToken,
    );
  });

  it("should publish results with nested attachments to server", async () => {
    const fileGlob = "src/__tests__/resultsDir1/*.xml";
    const attachmentGlob = "src/__tests__/attachmentsNestedDir/**/*";
    const serverUrl = "http://localhost:8080";

    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "DEF567", uri: "/tests/DEF567" });

    mockAxios
      .onPost(
        "http://localhost:8080/run/DEF567/attachments/attachmentNested1.txt",
      )
      .reply(200);

    mockAxios
      .onPost(
        "http://localhost:8080/run/DEF567/attachments/attachmentNested2.txt",
      )
      .reply(200);

    await collectAndSendResults(serverUrl, null, [fileGlob], [attachmentGlob]);

    expect(mockAxios.history.post.length).toBe(3);

    const attachment1PostRequest = mockAxios.history.post.find((postRequest) =>
      postRequest.url.includes("attachments/attachmentNested1.txt"),
    );
    expect(attachment1PostRequest).not.toBeNull();

    const attachment2PostRequest = mockAxios.history.post.find((postRequest) =>
      postRequest.url.includes("attachments/attachmentNested2.txt"),
    );
    expect(attachment2PostRequest).not.toBeNull();
  });

  it("when publishing attachments fails should not fail build", async () => {
    const fileGlob = "src/__tests__/resultsDir1/*.xml";
    const attachmentGlob = "src/__tests__/attachmentsNestedDir/**/*";
    const serverUrl = "http://localhost:8080";

    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "FAIL123", uri: "/tests/FAIL123" });

    mockAxios
      .onPost(
        "http://localhost:8080/run/FAIL123/attachments/attachmentNested1.txt",
      )
      .reply(400, null);

    await collectAndSendResults(serverUrl, null, [fileGlob], [attachmentGlob]);

    expect(mockAxios.history.post.length).toBe(3);

    const attachment1PostRequest = mockAxios.history.post.find((postRequest) =>
      postRequest.url.includes("attachments/attachmentNested1.txt"),
    );
    expect(attachment1PostRequest).not.toBeNull();
  });
});
