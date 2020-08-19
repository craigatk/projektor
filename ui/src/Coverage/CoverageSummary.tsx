import * as React from "react";
import PageTitle from "../PageTitle";
import LoadingState from "../Loading/LoadingState";
import { CoverageStats } from "../model/TestRunModel";
import { fetchOverallCoverage } from "../service/TestRunService";
import CoverageGraph from "./CoverageGraph";
import { Grid, makeStyles } from "@material-ui/core";

interface CoverageSummaryProps {
  publicId: string;
}

const useStyles = makeStyles({
  graphGrid: {
    paddingLeft: "5px",
    paddingRight: "20px",
  },
});

const CoverageSummary = ({ publicId }: CoverageSummaryProps) => {
  const classes = useStyles({});

  const [overallStats, setOverallStats] = React.useState<CoverageStats>(null);
  const [loadingState, setLoadingState] = React.useState(LoadingState.Loading);

  React.useEffect(() => {
    fetchOverallCoverage(publicId)
      .then((response) => {
        setOverallStats(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  }, [setOverallStats, setLoadingState]);

  if (overallStats) {
    return (
      <div>
        <PageTitle title="Coverage" testid="coverage-summary-title" />
        <Grid container className={classes.graphGrid}>
          <Grid item sm={4} xs={12} data-testid="coverage-summary-section-line">
            <CoverageGraph type="Line" coverageStat={overallStats.lineStat} />
          </Grid>
          <Grid
            item
            sm={4}
            xs={12}
            data-testid="coverage-summary-section-statement"
          >
            <CoverageGraph
              type="Statement"
              coverageStat={overallStats.statementStat}
            />
          </Grid>
          <Grid
            item
            sm={4}
            xs={12}
            data-testid="coverage-summary-section-branch"
          >
            <CoverageGraph
              type="Branch"
              coverageStat={overallStats.branchStat}
            />
          </Grid>
        </Grid>
      </div>
    );
  } else {
    return null;
  }
};

export default CoverageSummary;
