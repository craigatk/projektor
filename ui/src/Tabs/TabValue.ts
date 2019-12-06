import { WindowLocation } from "@reach/router";

const getTabCurrentValue = (location: WindowLocation, defaultTab: string) => {
  const lastPathElement = location.pathname.split("/").pop();
  const tabValue = lastPathElement !== "" ? `/${lastPathElement}` : defaultTab;

  return tabValue;
};

export { getTabCurrentValue };
