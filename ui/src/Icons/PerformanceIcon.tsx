import * as React from "react";
import AssessmentOutlinedIcon from "@mui/icons-material/AssessmentOutlined";

const PerformanceIcon = ({ className }: IconProps) => {
  return (
    <span title="Coverage">
      <AssessmentOutlinedIcon className={className} />
    </span>
  );
};

export default PerformanceIcon;
