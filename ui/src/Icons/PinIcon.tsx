import * as React from "react";
import BookmarkBorder from "@mui/icons-material/BookmarkBorder";

const PinIcon = ({ className }: IconProps) => {
  return (
    <span title="Pin">
      <BookmarkBorder className={className} />
    </span>
  );
};

export default PinIcon;
