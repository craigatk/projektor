function run(args) {
  const argv = require("minimist")(args);
  const { collectResults, sendResults } = require("./publish");

  const serverUrl = argv.serverUrl;
  const resultsFileGlobs = argv._;

  console.log(
    `Gathering results from ${resultsFileGlobs} to send to Projektor server ${serverUrl}`
  );

  const resultsBlob = collectResults(resultsFileGlobs);

  if (resultsBlob.length > 0) {
    sendResults(resultsBlob, serverUrl).then(respData => {
      console.log(`View Projektor results at ${serverUrl}${respData.uri}`);
    });
  }
}

module.exports = {
  run
};
