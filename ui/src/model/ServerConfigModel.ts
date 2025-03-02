interface ServerCleanupConfig {
  enabled: boolean;
  maxReportAgeInDays?: number;
}

interface AIServerConfig {
  testCaseFailureAnalysisEnabled: boolean;
}

interface ServerConfig {
  aiConfig: AIServerConfig;
  cleanup: ServerCleanupConfig;
}

export { AIServerConfig, ServerConfig, ServerCleanupConfig };
