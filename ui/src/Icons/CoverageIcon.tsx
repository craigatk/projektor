import * as React from "react";
import FileCopyOutlinedIcon from "@material-ui/icons/FileCopyOutlined";

const CoverageIcon = ({ className }: IconProps) => {
  return (
    <span title="Coverage">
      <FileCopyOutlinedIcon className={className} />
    </span>
  );
};

export default CoverageIcon;
