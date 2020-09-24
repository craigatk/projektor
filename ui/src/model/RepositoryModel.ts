import { CoverageStats } from "./TestRunModel";

interface RepositoryTimelineEntry {
  publicId: string;
  createdTimestamp: Date;
  cumulativeDuration: Date;
  totalTestCount: number;
}

interface RepositoryTimeline {
  timelineEntries: RepositoryTimelineEntry[];
}

interface RepositoryCoverageTimelineEntry {
  publicId: string;
  createdTimestamp: Date;
  coverageStats: CoverageStats;
}

interface RepositoryCoverageTimeline {
  timelineEntries: RepositoryCoverageTimelineEntry[];
}

export {
  RepositoryTimelineEntry,
  RepositoryTimeline,
  RepositoryCoverageTimeline,
  RepositoryCoverageTimelineEntry,
};
