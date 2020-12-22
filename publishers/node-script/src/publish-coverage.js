const { collectFileContents } = require("./file-utils");

const collectCoverage = (coverageFileGlobs, baseDirectoryPath) => {
  const coverageFiles = collectFileContents(coverageFileGlobs);

  return coverageFiles.map((coverageFile) => {
    const coverageFilePayload = {
      reportContents: coverageFile.contents.toString(),
      baseDirectoryPath,
    };

    return coverageFilePayload;
  });
};

module.exports = { collectCoverage };
