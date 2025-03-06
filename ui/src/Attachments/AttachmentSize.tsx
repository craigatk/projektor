import * as React from "react";
import { formatBytes } from "./byteFormat";

interface AttachmentSizeProps {
  fileSize?: number;
}

const AttachmentSize = ({ fileSize }: AttachmentSizeProps) => {
  return <span>{fileSize ? formatBytes(fileSize) : ""}</span>;
};

export default AttachmentSize;
