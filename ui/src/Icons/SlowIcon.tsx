import * as React from "react";
import HourglassEmpty from "@material-ui/icons/HourglassEmpty";

const SlowIcon = ({ className }: IconProps) => {
  return (
    <span title="Slow">
      <HourglassEmpty className={className} />
    </span>
  );
};

export default SlowIcon;
