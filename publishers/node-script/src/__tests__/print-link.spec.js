const { writeResultsFileToDisk } = require("../results-file");
const { printLinkFromFile } = require("../index");
const fs = require("fs");

describe("print link", () => {
  const resultsFileName = "projektor_report.json";

  let consoleLog;

  beforeEach(() => {
    consoleLog = jest.spyOn(console, "log").mockImplementation();
  });

  afterEach(() => {
    consoleLog.mockRestore();

    if (fs.existsSync(resultsFileName)) {
      fs.unlinkSync(resultsFileName);
    }
  });

  it("should log link to Projektor test report when results file exists", () => {
    const publicId = "REPORT123";
    const reportUrl = "http://localhost:8080/tests/REPORT123";

    writeResultsFileToDisk(publicId, reportUrl, resultsFileName);

    const returnedReportUrl = printLinkFromFile();

    expect(returnedReportUrl).toBe(reportUrl);

    expect(consoleLog).toHaveBeenLastCalledWith(
      "View Projektor results at http://localhost:8080/tests/REPORT123"
    );
  });

  it("should not log link to Projektor test report when results file does not exist", () => {
    const returnedReportUrl = printLinkFromFile();

    expect(returnedReportUrl).toBeNull();

    expect(consoleLog).toHaveBeenLastCalledWith(
      `No Projektor results file found with name ${resultsFileName}`
    );
  });
});
