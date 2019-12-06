const glob = require("glob");
const axios = require("axios");
const fs = require("fs");

const collectResults = resultsFileGlobs => {
  let resultsBlob = "";

  const resultsFileNames = [];

  resultsFileGlobs.forEach(fileGlob => {
    const fileNames = glob.sync(fileGlob);
    if (fileNames && fileNames.length > 0) {
      resultsFileNames.push(...fileNames);
    }
  });

  if (resultsFileNames.length > 0) {
    resultsFileNames.forEach(fileName => {
      const fileContents = fs.readFileSync(fileName);
      resultsBlob = resultsBlob + fileContents + "\n";
    });
  }

  return resultsBlob;
};

const sendResults = (resultsBlob, serverUrl) => {
  return axios
    .post(`${serverUrl}/results`, resultsBlob)
    .then(resp => Promise.resolve(resp.data))
    .catch(err => Promise.reject(err));
};

module.exports = {
  collectResults,
  sendResults
};
