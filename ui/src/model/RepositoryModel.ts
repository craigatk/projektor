import { CoverageStats } from "./TestRunModel";

interface RepositoryCoverageTimelineEntry {
  publicId: string;
  createdTimestamp: Date;
  coverageStats: CoverageStats;
}

interface RepositoryCoverageTimeline {
  timelineEntries: RepositoryCoverageTimelineEntry[];
}

export { RepositoryCoverageTimeline, RepositoryCoverageTimelineEntry };
