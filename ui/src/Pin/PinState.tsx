import * as React from "react";
import { ServerCleanupConfig } from "../model/ServerConfigModel";
import { fetchServerConfig } from "../service/ServerConfigService";
import {
  fetchTestRunSystemAttributes,
  pinTestRun,
  unpinTestRun,
} from "../service/TestRunService";

interface PinContextProps {
  pinned: boolean;
  togglePinned: () => void;
  cleanupEnabled: boolean;
  maxReportAgeInDays?: number;
}

export const PinContext = React.createContext<PinContextProps>(null);

export const PinState = ({ publicId, children }) => {
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

  const togglePinned = () => {
    if (pinned) {
      unpinTestRun(publicId).then(() => setPinned(false));
    } else {
      pinTestRun(publicId).then(() => setPinned(true));
    }
  };

  return (
    <PinContext.Provider
      value={{
        pinned,
        togglePinned,
        cleanupEnabled: cleanupConfig && cleanupConfig.enabled,
        maxReportAgeInDays: cleanupConfig
          ? cleanupConfig.maxReportAgeInDays
          : null,
      }}
    >
      {children}
    </PinContext.Provider>
  );
};
