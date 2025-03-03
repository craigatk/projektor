import * as React from "react";
import { AIServerConfig } from "../model/ServerConfigModel";
import { fetchServerConfig } from "../service/ServerConfigService";

interface AIContextProps {
  aiConfig: AIServerConfig;
}

export const AIContext = React.createContext<AIContextProps>(null);

export const AIState = ({ children }) => {
  const [aiConfig, setAiConfig] = React.useState<AIServerConfig>(null);

  React.useEffect(() => {
    fetchServerConfig()
      .then((response) => {
        setAiConfig(response.data.aiConfig);
      })
      .catch(() => {});
  }, [setAiConfig]);

  return (
    <AIContext.Provider
      value={{
        aiConfig,
      }}
    >
      {children}
    </AIContext.Provider>
  );
};
