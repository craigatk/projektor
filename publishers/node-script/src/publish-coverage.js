const axios = require("axios");
const { gzip } = require("node-gzip");
const { collectFileContents } = require("./file-utils");

const sendCoverage = async (
  serverUrl,
  publicId,
  publishToken,
  coverageFileContents,
  baseDirectoryPath,
  compressionEnabled
) => {
  const headers = {
    "Content-Type": "application/json",
  };

  if (publishToken) {
    headers["X-PROJEKTOR-TOKEN"] = publishToken;
  }

  if (compressionEnabled) {
    headers["Content-Encoding"] = "gzip";
  }

  const axiosInstance = axios.create({
    headers,
  });

  const coverageFilePayload = {
    reportContents: coverageFileContents,
    baseDirectoryPath,
  };

  const postUrl = `${serverUrl}/run/${publicId}/coverageFile`;
  const postData = compressionEnabled
    ? await gzip(JSON.stringify(coverageFilePayload))
    : coverageFilePayload;

  return axiosInstance
    .post(postUrl, postData)
    .then((resp) => Promise.resolve(resp.data))
    .catch((err) => Promise.reject(err));
};

const collectAndSendCoverage = async (
  serverUrl,
  publishToken,
  coverageFileGlobs,
  publicId,
  baseDirectoryPath,
  compressionEnabled
) => {
  if (coverageFileGlobs && coverageFileGlobs.length > 0) {
    const coverageFiles = collectFileContents(coverageFileGlobs);
    const coverageCount = coverageFiles.length;

    if (coverageCount) {
      console.log(
        `Sending ${coverageCount} coverage result(s) to Projektor server`
      );
      await Promise.all(
        coverageFiles.map((coverageFile) =>
          sendCoverage(
            serverUrl,
            publicId,
            publishToken,
            coverageFile.contents.toString(),
            baseDirectoryPath,
            compressionEnabled
          ).catch((e) => {
            console.error(
              `Error sending coverage result ${coverageFile.name} to Projektor server ${serverUrl}`,
              e.message
            );

            if (e.response && e.response.data) {
              console.error("Error from server", e.response.data.error_message);
            }
          })
        )
      );
      console.log(
        `Finished sending coverage ${coverageCount} results to Projektor`
      );
    }
  }
};

module.exports = { collectAndSendCoverage };
