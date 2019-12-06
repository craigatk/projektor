import * as React from "react";
import RadioButtonUncheckedIcon from "@material-ui/icons/RadioButtonUnchecked";

const SkippedIcon = ({ className }: IconProps) => {
  return (
    <span title="Skipped">
      <RadioButtonUncheckedIcon className={className} />
    </span>
  );
};

export default SkippedIcon;
