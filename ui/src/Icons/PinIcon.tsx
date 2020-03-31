import * as React from "react";
import BookmarkBorder from "@material-ui/icons/BookmarkBorder";

const PinIcon = ({ className }: IconProps) => {
  return (
    <span title="Pin">
      <BookmarkBorder className={className} />
    </span>
  );
};

export default PinIcon;
