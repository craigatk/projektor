import * as React from "react";
import CheckCircleOutlineIcon from "@material-ui/icons/CheckCircleOutline";

const PassedIcon = ({ className }: IconProps) => {
  return (
    <span title="Passed">
      <CheckCircleOutlineIcon className={className} />
    </span>
  );
};

export default PassedIcon;
