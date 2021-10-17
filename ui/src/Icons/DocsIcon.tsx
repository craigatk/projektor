import * as React from "react";
import AssignmentOutlined from "@material-ui/icons/AssignmentOutlined";

const DocsIcon = ({ className }: IconProps) => {
  return (
    <span title="Docs">
      <AssignmentOutlined className={className} />
    </span>
  );
};

export default DocsIcon;
