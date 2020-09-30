import * as React from "react";
import AcUnitOutlinedIcon from "@material-ui/icons/AcUnitOutlined";

const FlakyTestsIcon = ({ className }: IconProps) => {
  return (
    <span title="Flaky tests">
      <AcUnitOutlinedIcon className={className} />
    </span>
  );
};

export default FlakyTestsIcon;
