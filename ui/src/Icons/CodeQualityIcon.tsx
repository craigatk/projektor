import * as React from "react";
import AssignmentLateOutlinedIcon from "@material-ui/icons/AssignmentLateOutlined";

const CodeQualityIcon = ({ className }: IconProps) => {
  return (
    <span title="CodeQuality">
      <AssignmentLateOutlinedIcon className={className} />
    </span>
  );
};

export default CodeQualityIcon;
