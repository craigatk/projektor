import { CoverageStat, CoverageStats } from "../model/TestRunModel";

const createCoverageStats = (lineCoveredPercentage: number): CoverageStats => {
  const lineStat = {
    covered: 8,
    missed: 3,
    total: 11,
    coveredPercentage: lineCoveredPercentage,
  } as CoverageStat;

  const branchStat = {
    covered: 9,
    missed: 2,
    total: 11,
    coveredPercentage: lineCoveredPercentage + 1,
  } as CoverageStat;

  const statementStat = {
    covered: 10,
    missed: 1,
    total: 11,
    coveredPercentage: lineCoveredPercentage + 2,
  } as CoverageStat;

  return {
    lineStat,
    branchStat,
    statementStat,
  } as CoverageStats;
};

export { createCoverageStats };
