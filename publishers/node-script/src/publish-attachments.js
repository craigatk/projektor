const axios = require("axios");
const { collectFileContents } = require("./file-utils");

const sendAttachment = (
  serverUrl,
  publicId,
  publishToken,
  attachmentContents,
  attachmentFileName,
  attachmentMaxSizeMB,
) => {
  const headers = {};

  if (publishToken) {
    headers["X-PROJEKTOR-TOKEN"] = publishToken;
  }

  const axiosInstance = axios.create({
    headers,
  });

  const attachmentPostUrl = `${serverUrl}/run/${publicId}/attachments/${attachmentFileName}`;

  const attachmentMaxSize = attachmentMaxSizeMB * 1024 * 1024;

  return axiosInstance
    .post(attachmentPostUrl, attachmentContents, {
      maxBodyLength: attachmentMaxSize,
    })
    .then((resp) => Promise.resolve(resp.data))
    .catch((err) => Promise.reject(err));
};

const collectAndSendAttachments = (
  serverUrl,
  publishToken,
  attachmentFileGlobs,
  publicId,
  attachmentMaxSizeMB,
) => {
  if (attachmentFileGlobs && attachmentFileGlobs.length > 0) {
    const attachments = collectFileContents(attachmentFileGlobs);
    const attachmentsCount = attachments.length;

    if (attachmentsCount) {
      console.log(
        `Sending ${attachmentsCount} attachments to Projektor server`,
      );
      attachments.forEach((attachment) =>
        sendAttachment(
          serverUrl,
          publicId,
          publishToken,
          attachment.contents,
          attachment.name,
          attachmentMaxSizeMB,
        ).catch((e) => {
          console.error(
            `Error sending attachment ${attachment.name} to Projektor server ${serverUrl}`,
            e.message,
          );
        }),
      );
      console.log(
        `Finished sending attachments ${attachmentsCount} to Projektor`,
      );
    }
  }
};

module.exports = { collectAndSendAttachments };
