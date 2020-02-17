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

const sendResults = (resultsBlob, serverUrl, publishToken) => {
  const headers = {};

  if (publishToken) {
    headers["X-PROJEKTOR-TOKEN"] = publishToken;
  }

  const axiosInstance = axios.create({
    headers
  });

  return axiosInstance
    .post(`${serverUrl}/results`, resultsBlob)
    .then(resp => Promise.resolve(resp.data))
    .catch(err => Promise.reject(err));
};

const collectAndSendResults = (serverUrl, publishToken, resultsFileGlobs) => {
  console.log(
    `Gathering results from ${resultsFileGlobs} to send to Projektor server ${serverUrl}`
  );

  const resultsBlob = collectResults(resultsFileGlobs);

  if (resultsBlob.length > 0) {
    sendResults(resultsBlob, serverUrl, publishToken)
      .then(respData => {
        console.log(`View Projektor results at ${serverUrl}${respData.uri}`);
      })
      .catch(e => {
        console.error(
          `Error publishing results to Projektor server ${serverUrl}`,
          e.message
        );
      });
  }
};

module.exports = {
  collectResults,
  sendResults,
  collectAndSendResults
};
