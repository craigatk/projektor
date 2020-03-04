const glob = require("glob");
const axios = require("axios");
const fs = require("fs");
const path = require("path");

const isFile = path => fs.lstatSync(path).isFile();

const globsToFilePaths = fileGlobs => {
  const allFilePaths = [];

  fileGlobs.forEach(fileGlob => {
    const filePaths = glob.sync(fileGlob);
    if (filePaths && filePaths.length > 0) {
      allFilePaths.push(...filePaths);
    }
  });

  return allFilePaths.filter(filePath => isFile(filePath));
};

const collectResults = resultsFileGlobs => {
  let resultsBlob = "";

  const resultsFilePaths = globsToFilePaths(resultsFileGlobs);

  if (resultsFilePaths.length > 0) {
    resultsFilePaths.forEach(filePath => {
      const fileContents = fs.readFileSync(filePath);
      resultsBlob = resultsBlob + fileContents + "\n";
    });
  }

  return resultsBlob;
};

const collectAttachments = attachmentFileGlobs => {
  const attachmentFilePaths = globsToFilePaths(attachmentFileGlobs);

  const attachments = attachmentFilePaths.map(filePath => {
    const fileContents = fs.readFileSync(filePath);
    const fileName = path.basename(filePath);
    return { name: fileName, contents: fileContents };
  });

  return attachments;
};

const sendResults = (serverUrl, publishToken, resultsBlob) => {
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

const sendAttachment = (
  serverUrl,
  publicId,
  publishToken,
  attachmentContents,
  attachmentFileName
) => {
  const headers = {};

  if (publishToken) {
    headers["X-PROJEKTOR-TOKEN"] = publishToken;
  }

  const axiosInstance = axios.create({
    headers
  });

  const attachmentPostUrl = `${serverUrl}/run/${publicId}/attachments/${attachmentFileName}`;

  return axiosInstance
    .post(attachmentPostUrl, attachmentContents)
    .then(resp => Promise.resolve(resp.data))
    .catch(err => Promise.reject(err));
};

const collectAndSendResults = (
  serverUrl,
  publishToken,
  resultsFileGlobs,
  attachmentFileGlobs
) => {
  console.log(
    `Gathering results from ${resultsFileGlobs} to send to Projektor server ${serverUrl}`
  );

  const resultsBlob = collectResults(resultsFileGlobs);

  if (resultsBlob.length > 0) {
    sendResults(serverUrl, publishToken, resultsBlob)
      .then(respData => {
        console.log(`View Projektor results at ${serverUrl}${respData.uri}`);

        return Promise.resolve(respData.id);
      })
      .then(publicId => {
        if (attachmentFileGlobs && attachmentFileGlobs.length > 0) {
          const attachments = collectAttachments(attachmentFileGlobs);
          const attachmentsCount = attachments.length;

          if (attachmentsCount) {
            console.log(
              `Sending ${attachmentsCount} attachments to Projektor server`
            );
            attachments.forEach(attachment =>
              sendAttachment(
                serverUrl,
                publicId,
                publishToken,
                attachment.contents,
                attachment.name
              )
            );
            console.log(
              `Finished sending attachments ${attachmentsCount} to Projektor`
            );
          }
        }
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
