import * as React from "react";
import HourglassEmpty from "@mui/icons-material/HourglassEmpty";

const SlowIcon = ({ className }: IconProps) => {
  return (
    <span title="Slow">
      <HourglassEmpty className={className} />
    </span>
  );
};

export default SlowIcon;
