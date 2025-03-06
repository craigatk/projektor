import prettyBytes from "pretty-bytes";

const formatBytes = (fileSize?: number): string => {
  if (fileSize) {
    return prettyBytes(fileSize);
  } else {
    return "";
  }
};

export { formatBytes };
