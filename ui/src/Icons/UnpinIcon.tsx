import * as React from "react";
import Bookmark from "@material-ui/icons/Bookmark";

const UnpinIcon = ({ className }: IconProps) => {
  return (
    <span title="Unpin">
      <Bookmark className={className} />
    </span>
  );
};

export default UnpinIcon;
