import * as React from "react";
import LoadingSection from "../Loading/LoadingSection";
import TestCaseFailurePanelList from "./TestCaseFailurePanelList";
import { fetchFailedTestCases } from "../service/TestRunService";
import LoadingState from "../Loading/LoadingState";
import { RouteComponentProps } from "@reach/router";
import PageTitle from "../PageTitle";
import { makeStyles } from "@material-ui/styles";

interface FailedTestCasesProps extends RouteComponentProps {
  publicId: string;
}

const useStyles = makeStyles(() => ({
  mainSection: {
    marginTop: "20px",
  },
}));

const FailedTestCases = ({ publicId }: FailedTestCasesProps) => {
  const classes = useStyles({});

  const [failedTestCases, setFailedTestCases] = React.useState([]);
  const [loadingState, setLoadingState] = React.useState(LoadingState.Loading);

  React.useEffect(() => {
    fetchFailedTestCases(publicId).then((response) => {
      setFailedTestCases(response.data);
      setLoadingState(LoadingState.Success);
    });
  }, [setFailedTestCases, setLoadingState]);

  return (
    <div className={classes.mainSection}>
      <PageTitle title="Failed tests" testid="failed-tests-title" />
      <LoadingSection
        loadingState={loadingState}
        successComponent={
          <TestCaseFailurePanelList
            failedTestCases={failedTestCases}
            publicId={publicId}
          />
        }
      />
    </div>
  );
};

export default FailedTestCases;
