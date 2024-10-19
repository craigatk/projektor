import * as React from "react";
import RadioButtonUncheckedIcon from "@mui/icons-material/RadioButtonUnchecked";

const SkippedIcon = ({ className }: IconProps) => {
  return (
    <span title="Skipped">
      <RadioButtonUncheckedIcon className={className} />
    </span>
  );
};

export default SkippedIcon;
