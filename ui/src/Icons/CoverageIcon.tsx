import * as React from "react";
import FileCopyOutlinedIcon from "@mui/icons-material/FileCopyOutlined";

const CoverageIcon = ({ className }: IconProps) => {
  return (
    <span title="Coverage">
      <FileCopyOutlinedIcon className={className} />
    </span>
  );
};

export default CoverageIcon;
