import * as React from "react";
import Storage from "@mui/icons-material/Storage";

const TotalIcon = ({ className }: IconProps) => {
  return (
    <span title="Total">
      <Storage className={className} />
    </span>
  );
};

export default TotalIcon;
