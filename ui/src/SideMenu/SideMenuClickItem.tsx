import * as React from "react";
import classes from "./SideMenuClickItem.module.css";
import SideMenuItem from "./SideMenuItem";

interface SideMenuClickItemProps {
  onClick: () => void;
  icon: any;
  text: string;
  testId: string;
}

const SideMenuClickItem = ({
  onClick,
  icon,
  text,
  testId,
}: SideMenuClickItemProps) => {
  return (
    <div onClick={onClick} className={classes.sideNavLink}>
      <SideMenuItem icon={icon} text={text} testId={testId} />
    </div>
  );
};

export default SideMenuClickItem;
