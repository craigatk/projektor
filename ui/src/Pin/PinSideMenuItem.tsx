import * as React from "react";
import { ServerCleanupConfig } from "../model/ServerConfigModel";
import { fetchServerConfig } from "../service/ServerConfigService";
import {
  fetchTestRunSystemAttributes,
  pinTestRun,
  unpinTestRun,
} from "../service/TestRunService";
import SideMenuClickItem from "../SideMenu/SideMenuClickItem";
import UnpinIcon from "../Icons/UnpinIcon";
import PinIcon from "../Icons/PinIcon";

interface PinSideMenuItem {
  publicId: string;
}

const PinSideMenuItem = ({ publicId }: PinSideMenuItem) => {
  const [cleanupConfig, setCleanupConfig] = React.useState<ServerCleanupConfig>(
    null
  );
  const [pinned, setPinned] = React.useState<boolean>(false);

  React.useEffect(() => {
    fetchServerConfig()
      .then((response) => {
        setCleanupConfig(response.data.cleanup);
        return Promise.resolve(response.data.cleanup.enabled);
      })
      .then((enabled) => {
        if (enabled) {
          fetchTestRunSystemAttributes(publicId)
            .then((attributesResponse) => {
              setPinned(attributesResponse.data.pinned);
            })
            .catch(() => {});
        }
      })
      .catch(() => {});
  }, [setCleanupConfig, setPinned]);

  const onClick = () => {
    if (pinned) {
      unpinTestRun(publicId).then(() => setPinned(false));
    } else {
      pinTestRun(publicId).then(() => setPinned(true));
    }
  };

  if (cleanupConfig && cleanupConfig.enabled) {
    return (
      <SideMenuClickItem
        onClick={onClick}
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
