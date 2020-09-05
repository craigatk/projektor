const { writeResultsFileToDisk } = require("../results-file");

const fs = require("fs");

describe("results file", () => {
  const resultsFileName = "projektor_report.json";

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

    const resultsFileJson = JSON.parse(resultsFileContents);

    expect(resultsFileJson.id).toBe(publicId);
    expect(resultsFileJson.reportUrl).toBe(reportUrl);
  });
});
