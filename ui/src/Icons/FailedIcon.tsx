import * as React from "react";
import Error from "@mui/icons-material/Error";

const FailedIcon = ({ className }: IconProps) => {
  return (
    <span title="Failed">
      <Error className={className} />
    </span>
  );
};

export default FailedIcon;
