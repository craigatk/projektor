import * as React from "react";
import AssignmentOutlined from "@mui/icons-material/AssignmentOutlined";

const DocsIcon = ({ className }: IconProps) => {
  return (
    <span title="Docs">
      <AssignmentOutlined className={className} />
    </span>
  );
};

export default DocsIcon;
