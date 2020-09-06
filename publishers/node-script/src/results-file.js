const fs = require("fs");

const defaultResultsFileName = "projektor_report.json";

function writeResultsFileToDisk(publicId, reportUrl, resultsFileName) {
  const resultsFileJson = {
    id: publicId,
    report_url: reportUrl,
  };

  fs.writeFileSync(resultsFileName, JSON.stringify(resultsFileJson));
}

function readResultsFileFromDisk(resultsFileName) {
  const resultsFileContents = fs.readFileSync(resultsFileName).toString();
  const results = JSON.parse(resultsFileContents);

  return { id: results.id, reportUrl: results.report_url };
}

module.exports = {
  writeResultsFileToDisk,
  readResultsFileFromDisk,
  defaultResultsFileName,
};
