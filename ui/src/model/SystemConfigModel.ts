interface SystemCleanupConfig {
  enabled: boolean;
  maxReportAgeInDays?: number;
}

interface SystemConfig {
  cleanup: SystemCleanupConfig;
}

export { SystemConfig, SystemCleanupConfig };
