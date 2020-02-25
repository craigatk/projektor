import * as React from "react";
import InsertDriveFileOutlinedIcon from "@material-ui/icons/InsertDriveFileOutlined";

const AttachmentIcon = ({ className }: IconProps) => {
  return (
    <span title="Attachment">
      <InsertDriveFileOutlinedIcon className={className} />
    </span>
  );
};

export default AttachmentIcon;
