const axios = require("axios");
const MockAdapter = require("axios-mock-adapter");
const { collectAndSendResults } = require("../publish");

describe("Publishing invalid results", () => {
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

  it("should log error message from server when publishing invalid results", async () => {
    const fileGlob = "src/__tests__/resultsDir1/*.xml";
    const serverUrl = "http://localhost:8080";

    mockAxios
      .onPost("http://localhost:8080/groupedResults")
      .reply(400, { error_message: "Failed to parse" });

    await collectAndSendResults(serverUrl, null, [fileGlob]);

    expect(consoleError).toHaveBeenCalledWith(
      "Error publishing results to Projektor server http://localhost:8080",
      "Request failed with status code 400"
    );

    expect(consoleError).toHaveBeenCalledWith(
      "Error from server",
      "Failed to parse"
    );
  });
});
