const glob = require("glob");
const axios = require("axios");
const fs = require("fs");
const path = require("path");

const isFile = (path) => fs.lstatSync(path).isFile();

const globsToFilePaths = (fileGlobs) => {
  const allFilePaths = [];

  fileGlobs.forEach((fileGlob) => {
    const filePaths = glob.sync(fileGlob);
    if (filePaths && filePaths.length > 0) {
      allFilePaths.push(...filePaths);
    }
  });

  return allFilePaths.filter((filePath) => isFile(filePath));
};

const collectResults = (resultsFileGlobs) => {
  let resultsBlob = "";

  const resultsFilePaths = globsToFilePaths(resultsFileGlobs);

  if (resultsFilePaths.length > 0) {
    resultsFilePaths.forEach((filePath) => {
      const fileContents = fs.readFileSync(filePath);
      resultsBlob = resultsBlob + fileContents + "\n";
    });
  }

  return resultsBlob;
};

const collectFileContents = (fileGlobs) => {
  const filePaths = globsToFilePaths(fileGlobs);

  return filePaths.map((filePath) => {
    const contents = fs.readFileSync(filePath);
    const name = path.basename(filePath);
    return { name, contents };
  });
};

const sendResults = async (serverUrl, publishToken, resultsBlob) => {
  const headers = {};

  if (publishToken) {
    headers["X-PROJEKTOR-TOKEN"] = publishToken;
  }

  const axiosInstance = axios.create({
    headers,
  });

  const resp = await axiosInstance.post(`${serverUrl}/results`, resultsBlob);

  return resp.data;
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
    headers,
  });

  const attachmentPostUrl = `${serverUrl}/run/${publicId}/attachments/${attachmentFileName}`;

  return axiosInstance
    .post(attachmentPostUrl, attachmentContents)
    .then((resp) => Promise.resolve(resp.data))
    .catch((err) => Promise.reject(err));
};

const sendCoverage = (
  serverUrl,
  publicId,
  publishToken,
  coverageFileContents
) => {
  const headers = {};

  if (publishToken) {
    headers["X-PROJEKTOR-TOKEN"] = publishToken;
  }

  const axiosInstance = axios.create({
    headers,
  });

  const postUrl = `${serverUrl}/run/${publicId}/coverage`;

  return axiosInstance
    .post(postUrl, coverageFileContents)
    .then((resp) => Promise.resolve(resp.data))
    .catch((err) => Promise.reject(err));
};

const collectAndSendAttachments = (
  serverUrl,
  publishToken,
  attachmentFileGlobs,
  publicId
) => {
  if (attachmentFileGlobs && attachmentFileGlobs.length > 0) {
    const attachments = collectFileContents(attachmentFileGlobs);
    const attachmentsCount = attachments.length;

    if (attachmentsCount) {
      console.log(
        `Sending ${attachmentsCount} attachments to Projektor server`
      );
      attachments.forEach((attachment) =>
        sendAttachment(
          serverUrl,
          publicId,
          publishToken,
          attachment.contents,
          attachment.name
        ).catch((e) => {
          console.error(
            `Error sending attachment ${attachment.name} to Projektor server ${serverUrl}`,
            e.message
          );
        })
      );
      console.log(
        `Finished sending attachments ${attachmentsCount} to Projektor`
      );
    }
  }
};

const collectAndSendCoverage = (
  serverUrl,
  publishToken,
  coverageFileGlobs,
  publicId
) => {
  if (coverageFileGlobs && coverageFileGlobs.length > 0) {
    const coverageFiles = collectFileContents(coverageFileGlobs);
    const coverageCount = coverageFiles.length;

    if (coverageCount) {
      console.log(
        `Sending ${coverageCount} coverage result(s) to Projektor server`
      );
      coverageFiles.forEach((coverageFile) =>
        sendCoverage(
          serverUrl,
          publicId,
          publishToken,
          coverageFile.contents
        ).catch((e) => {
          console.error(
            `Error sending coverage result ${coverageFile} to Projektor server ${serverUrl}`,
            e.message
          );
        })
      );
      console.log(
        `Finished sending coverage ${coverageCount} results to Projektor`
      );
    }
  }
};

const collectAndSendResults = async (
  serverUrl,
  publishToken,
  resultsFileGlobs,
  attachmentFileGlobs,
  coverageFileGlobs
) => {
  console.log(
    `Gathering results from ${resultsFileGlobs} to send to Projektor server ${serverUrl}`
  );

  const resultsBlob = collectResults(resultsFileGlobs);

  if (resultsBlob.length > 0) {
    try {
      const resultsResponseData = await sendResults(
        serverUrl,
        publishToken,
        resultsBlob
      );

      const publicId = resultsResponseData.id;
      const reportUrl = `${serverUrl}${resultsResponseData.uri}`;
      console.log(`View Projektor results at ${reportUrl}`);

      await collectAndSendAttachments(
        serverUrl,
        publishToken,
        attachmentFileGlobs,
        publicId
      );

      await collectAndSendCoverage(
        serverUrl,
        publishToken,
        coverageFileGlobs,
        publicId
      );

      return { resultsBlob, publicId, reportUrl };
    } catch (e) {
      console.error(
        `Error publishing results to Projektor server ${serverUrl}`,
        e.message
      );
      return { resultsBlob, publicId: null, reportUrl: null };
    }
  } else {
    return { resultsBlob, publicId: null, reportUrl: null };
  }
};

module.exports = {
  collectResults,
  sendResults,
  collectAndSendResults,
};
