const {
  writeResultsFileToDisk,
  readResultsFileFromDisk,
  defaultResultsFileName,
} = require("../results-file");

const fs = require("fs");

describe("results file", () => {
  const resultsFileName = "projektor_results_test.json";

  afterEach(() => {
    if (fs.existsSync(resultsFileName)) {
      fs.unlinkSync(resultsFileName);
    }
  });

  it("should write file with id and url", () => {
    const publicId = "RES123";
    const reportUrl = "http://localhost:8080/tests/RES123";

    writeResultsFileToDisk(publicId, reportUrl, resultsFileName);

    const resultsFileContents = fs.readFileSync(resultsFileName).toString();
    console.log("Results file contents", resultsFileContents);

    expect(resultsFileContents).toContain("report_url");

    const resultsFileJson = JSON.parse(resultsFileContents);

    expect(resultsFileJson.id).toBe(publicId);
    expect(resultsFileJson.report_url).toBe(reportUrl);
  });

  it("should read results from file", () => {
    const publicId = "RES123";
    const reportUrl = "http://localhost:8080/tests/RES123";

    writeResultsFileToDisk(publicId, reportUrl, resultsFileName);

    const results = readResultsFileFromDisk(resultsFileName);

    expect(results.id).toBe(publicId);
    expect(results.reportUrl).toBe(reportUrl);
  });
});
