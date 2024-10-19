import * as React from "react";
import TimelineOutlinedIcon from "@mui/icons-material/TimelineOutlined";

const RepositoryIcon = ({ className }: IconProps) => {
  return (
    <span title="Repository">
      <TimelineOutlinedIcon className={className} />
    </span>
  );
};

export default RepositoryIcon;
