import * as React from "react";
import ElectricBolt from "@mui/icons-material/ElectricBolt";

const AIIcon = ({ className }: IconProps) => {
  return (
    <span>
      <ElectricBolt className={className} />
    </span>
  );
};

export default AIIcon;
