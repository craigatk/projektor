const extractTestRunId = (stdOut) => {
  const startingIndex = stdOut.indexOf("/tests/");

  return stdOut.substr(startingIndex + "/tests/".length, 12);
};

module.exports = {
  extractTestRunId,
};
