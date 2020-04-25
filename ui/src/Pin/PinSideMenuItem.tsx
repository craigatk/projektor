import * as React from "react";
import SideMenuClickItem from "../SideMenu/SideMenuClickItem";
import UnpinIcon from "../Icons/UnpinIcon";
import PinIcon from "../Icons/PinIcon";
import { PinContext } from "./PinState";

const PinSideMenuItem = () => {
  const { togglePinned, pinned, cleanupEnabled } = React.useContext(PinContext);

  if (cleanupEnabled) {
    return (
      <SideMenuClickItem
        onClick={togglePinned}
        icon={pinned ? <UnpinIcon /> : <PinIcon />}
        text={pinned ? "Unpin" : "Pin"}
        testId={pinned ? "nav-link-unpin" : "nav-link-pin"}
      />
    );
  } else {
    return null;
  }
};

export default PinSideMenuItem;
