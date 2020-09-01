import * as React from "react";
import { CoverageStats } from "../model/TestRunModel";
import { Grid, makeStyles } from "@material-ui/core";
import CoverageGraph from "./CoverageGraph";

interface OverallCoverageGraphsProps {
  overallStats: CoverageStats;
  previousTestRunId?: string;
}

const useStyles = makeStyles({
  graphGrid: {
    marginLeft: "10px",
    paddingLeft: "5px",
    paddingRight: "20px",
    marginBottom: "15px",
  },
});

const OverallCoverageGraphs = ({
  overallStats,
  previousTestRunId,
}: OverallCoverageGraphsProps) => {
  const classes = useStyles({});

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
      <Grid item sm={4} xs={12} data-testid="overall-coverage-section-branch">
        <CoverageGraph
          type="Branch"
          coverageStat={overallStats.branchStat}
          height={25}
          inline={false}
          previousTestRunId={previousTestRunId}
        />
      </Grid>
    </Grid>
  );
};

export default OverallCoverageGraphs;
