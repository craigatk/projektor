const fs = require("fs");

function writeResultsFileToDisk(publicId, reportUrl, resultsFileName) {
  const resultsFileJson = {
    id: publicId,
    reportUrl,
  };

  fs.writeFileSync(resultsFileName, JSON.stringify(resultsFileJson));
}

module.exports = {
  writeResultsFileToDisk,
};
