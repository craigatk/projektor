import * as React from "react";
import AssessmentOutlinedIcon from "@material-ui/icons/AssessmentOutlined";

const PerformanceIcon = ({ className }: IconProps) => {
  return (
    <span title="Coverage">
      <AssessmentOutlinedIcon className={className} />
    </span>
  );
};

export default PerformanceIcon;
