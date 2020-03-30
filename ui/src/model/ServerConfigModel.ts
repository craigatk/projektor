interface ServerCleanupConfig {
  enabled: boolean;
  maxReportAgeInDays?: number;
}

interface ServerConfig {
  cleanup: ServerCleanupConfig;
}

export { ServerConfig, ServerCleanupConfig };
