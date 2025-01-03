import * as React from "react";
import classes from "./OverallCoverageGraphs.module.css";
import { CoverageStats } from "../model/TestRunModel";
import { Grid } from "@mui/material";
import CoverageGraph from "./CoverageGraph";

interface OverallCoverageGraphsProps {
  overallStats: CoverageStats;
  previousTestRunId?: string;
}

const OverallCoverageGraphs = ({
  overallStats,
  previousTestRunId,
}: OverallCoverageGraphsProps) => {
  if (overallStats) {
    return (
      <Grid container className={classes.graphGrid}>
        <Grid item sm={4} xs={12} data-testid="overall-coverage-section-line">
          <CoverageGraph
            type="Line"
            coverageStat={overallStats.lineStat}
            height={25}
            inline={false}
            previousTestRunId={previousTestRunId}
          />
        </Grid>
        <Grid item sm={4} xs={12} data-testid="overall-coverage-section-branch">
          <CoverageGraph
            type="Branch"
            coverageStat={overallStats.branchStat}
            height={25}
            inline={false}
            previousTestRunId={previousTestRunId}
          />
        </Grid>
        <Grid
          item
          sm={4}
          xs={12}
          data-testid="overall-coverage-section-statement"
        >
          <CoverageGraph
            type="Statement"
            coverageStat={overallStats.statementStat}
            height={25}
            inline={false}
            previousTestRunId={previousTestRunId}
          />
        </Grid>
      </Grid>
    );
  } else {
    return null;
  }
};

export default OverallCoverageGraphs;
