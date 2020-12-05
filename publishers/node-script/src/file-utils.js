const glob = require("glob");
const fs = require("fs");
const path = require("path");
const _ = require("lodash");

const isFile = (path) => fs.lstatSync(path).isFile();

const globsToFilePaths = (fileGlobs) => {
  if (_.isNil(fileGlobs)) {
    return [];
  }

  const allFilePaths = [];

  fileGlobs.forEach((fileGlob) => {
    const filePaths = glob.sync(fileGlob);
    if (filePaths && filePaths.length > 0) {
      allFilePaths.push(...filePaths);
    }
  });

  return allFilePaths.filter((filePath) => isFile(filePath));
};

const collectFileContents = (fileGlobs) => {
  const filePaths = globsToFilePaths(fileGlobs);

  return filePaths.map((filePath) => {
    const contents = fs.readFileSync(filePath);
    const name = path.basename(filePath);
    return { name, contents };
  });
};

module.exports = { isFile, globsToFilePaths, collectFileContents };
