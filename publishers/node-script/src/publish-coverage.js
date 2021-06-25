const { collectFileContents } = require("./file-utils");

const collectCoverage = (coverageFileGlobs, baseDirectoryPath) => {
  const coverageFiles = collectFileContents(coverageFileGlobs);

  console.log(
    `Found ${coverageFiles.length} coverage file(s) in ${coverageFileGlobs}`
  );

  return coverageFiles.map((coverageFile) => {
    const coverageFilePayload = {
      reportContents: coverageFile.contents.toString(),
      baseDirectoryPath,
    };

    return coverageFilePayload;
  });
};

module.exports = { collectCoverage };
