import * as React from "react";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutlined";

const PassedIcon = ({ className }: IconProps) => {
  return (
    <span title="Passed">
      <CheckCircleOutlineIcon className={className} />
    </span>
  );
};

export default PassedIcon;
