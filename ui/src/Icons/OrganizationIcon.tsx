import * as React from "react";
import AccountTreeOutlinedIcon from "@material-ui/icons/AccountTreeOutlined";

const OrganizationIcon = ({ className }: IconProps) => {
  return (
    <span title="Organization">
      <AccountTreeOutlinedIcon className={className} />
    </span>
  );
};

export default OrganizationIcon;
