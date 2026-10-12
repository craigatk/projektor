interface ServerCleanupConfig {
  enabled: boolean;
  maxReportAgeInDays?: number;
}

interface AIServerConfig {
  testCaseFailureAnalysisEnabled: boolean;
}

interface SlackServerConfig {
  enabled: boolean;
}

interface ServerConfig {
  aiConfig: AIServerConfig;
  cleanup: ServerCleanupConfig;
  slack?: SlackServerConfig;
}

export { AIServerConfig, ServerConfig, ServerCleanupConfig, SlackServerConfig };
