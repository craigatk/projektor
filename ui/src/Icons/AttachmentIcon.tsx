import * as React from "react";
import InsertDriveFileOutlinedIcon from "@mui/icons-material/InsertDriveFileOutlined";

const AttachmentIcon = ({ className }: IconProps) => {
  return (
    <span title="Attachment">
      <InsertDriveFileOutlinedIcon className={className} />
    </span>
  );
};

export default AttachmentIcon;
