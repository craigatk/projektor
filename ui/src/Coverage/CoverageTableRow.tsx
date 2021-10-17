import { CoverageStats } from "../model/TestRunModel";

interface CoverageTableRow {
  name: string;
  stats: CoverageStats;
  coveredPercentageLink?: string;
  previousTestRunId?: string;
  nameLinkUrl?: string;
}

export default CoverageTableRow;
