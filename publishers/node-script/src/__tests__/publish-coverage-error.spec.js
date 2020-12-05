const axios = require("axios");
const MockAdapter = require("axios-mock-adapter");
const { collectAndSendResults } = require("../publish");

describe("publish invalid coverage", () => {
  let consoleError;
  let mockAxios;

  beforeEach(() => {
    consoleError = jest.spyOn(console, "error").mockImplementation();
    mockAxios = new MockAdapter(axios);
  });

  afterEach(() => {
    consoleError.mockRestore();
    mockAxios.restore();
  });

  it("should log error when publishing invalid coverage", async () => {
    const fileGlob = "src/__tests__/resultsDir1/*.xml";
    const coverageGlob = "src/__tests__/coverageDir1/*.xml";
    const serverUrl = "http://localhost:8080";

    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(200, { id: "ABC123", uri: "/tests/ABC123" });

    mockAxios
      .onPost("http://localhost:8080/run/ABC123/coverageFile")
      .reply(400, { error_message: "Failed to parse" });

    await collectAndSendResults(serverUrl, null, [fileGlob], null, [
      coverageGlob,
    ]);

    expect(consoleError).toHaveBeenCalledWith(
      "Error sending coverage result ui-clover.xml to Projektor server http://localhost:8080",
      "Request failed with status code 400"
    );

    expect(consoleError).toHaveBeenCalledWith(
      "Error from server",
      "Failed to parse"
    );
  });
});
