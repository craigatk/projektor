import * as React from "react";
import PageTitle from "../PageTitle";
import LoadingState from "../Loading/LoadingState";
import { Coverage, TestRunGitMetadata } from "../model/TestRunModel";
import { fetchCoverage } from "../service/TestRunService";
import OverallCoverageGraphs from "./OverallCoverageGraphs";
import CleanLink from "../Link/CleanLink";
import { makeStyles } from "@material-ui/styles";
import TestRunCoverageBadge from "../Badge/coverage/TestRunCoverageBadge";

interface CoverageSummaryProps {
  publicId: string;
  gitMetadata?: TestRunGitMetadata;
}

const useStyles = makeStyles(() => ({
  mainSection: {
    marginTop: "20px",
  },
  coverageBadgeSection: {
    marginLeft: "15px",
  },
}));

const CoverageSummary = ({ publicId, gitMetadata }: CoverageSummaryProps) => {
  const classes = useStyles({});

  const [coverage, setCoverage] = React.useState<Coverage>(null);
  const [loadingState, setLoadingState] = React.useState(LoadingState.Loading);

  React.useEffect(() => {
    fetchCoverage(publicId)
      .then((response) => {
        setCoverage(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  }, [setCoverage, setLoadingState]);

  if (coverage) {
    return (
      <div className={classes.mainSection}>
        <CleanLink to={`/tests/${publicId}/coverage`}>
          <PageTitle title="Coverage" testid="coverage-summary-title" />
        </CleanLink>

        <OverallCoverageGraphs
          overallStats={coverage.overallStats}
          previousTestRunId={coverage.previousTestRunId}
        />

        {gitMetadata && (
          <div className={classes.coverageBadgeSection}>
            <TestRunCoverageBadge
              publicId={publicId}
              repoName={gitMetadata.repoName}
              projectName={gitMetadata.projectName}
            />
          </div>
        )}
      </div>
    );
  } else {
    return null;
  }
};

export default CoverageSummary;
