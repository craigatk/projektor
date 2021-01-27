import * as React from "react";
import LoadingState from "../../Loading/LoadingState";
import { RouteComponentProps } from "@reach/router";
import { RepositoryFlakyTests } from "../../model/RepositoryModel";
import { fetchRepositoryFlakyTests } from "../../service/RepositoryService";
import LoadingSection from "../../Loading/LoadingSection";
import RepositoryFlakyTestsDetails from "./RepositoryFlakyTestsDetails";
import { Button, TextField, Tooltip } from "@material-ui/core";
import PageTitle from "../../PageTitle";
import { makeStyles } from "@material-ui/styles";

interface RepositoryFlakyTestsPageProps extends RouteComponentProps {
  orgPart: string;
  repoPart: string;
  projectName?: string;
  hideIfEmpty: boolean;
}

const useStyles = makeStyles(() => ({
  paramSection: {
    marginLeft: "10px",
    marginTop: "10px",
  },
  textField: {
    margin: "5px",
    width: "180px",
  },
  searchButton: {
    margin: "5px",
  },
}));

const RepositoryFlakyTestsPage = ({
  orgPart,
  repoPart,
  projectName,
  hideIfEmpty,
}: RepositoryFlakyTestsPageProps) => {
  const classes = useStyles({});

  const repoName = `${orgPart}/${repoPart}`;

  const [flakyTests, setFlakyTests] = React.useState<RepositoryFlakyTests>(
    null
  );
  const [maxRuns, setMaxRuns] = React.useState<number>(50);
  const [flakyThreshold, setFlakyThreshold] = React.useState<number>(5);
  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading
  );

  React.useEffect(() => {
    fetchRepositoryFlakyTests(maxRuns, flakyThreshold, repoName, projectName)
      .then((response) => {
        setFlakyTests(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  }, [setFlakyTests, setLoadingState]);

  const flakyThresholdOnChange = (e) => {
    const parsedValue = parseInt(e.target.value);

    if (parsedValue > 0) {
      setFlakyThreshold(parsedValue);
    }
  };

  const maxRunsOnChange = (e) => {
    const parsedValue = parseInt(e.target.value);

    if (parsedValue > 0) {
      setMaxRuns(parsedValue);
    }
  };

  const search = () => {
    setLoadingState(LoadingState.Loading);
    fetchRepositoryFlakyTests(maxRuns, flakyThreshold, repoName, projectName)
      .then((response) => {
        setFlakyTests(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  };

  return (
    <div>
      <PageTitle title="Flaky tests" testid="repository-flaky-tests-title" />

      <div className={classes.paramSection}>
        <Tooltip
          title="How many times a test has to fail to be considered flaky"
          placement="top"
        >
          <TextField
            label="Flaky test threshold"
            data-testid="flaky-tests-threshold"
            className={classes.textField}
            defaultValue="5"
            variant="outlined"
            size="small"
            onChange={flakyThresholdOnChange}
          />
        </Tooltip>

        <Tooltip
          title="How many test runs to look back and examine when looking for flaky tests"
          placement="top"
        >
          <TextField
            label="How many tests back"
            data-testid="flaky-tests-max-runs"
            className={classes.textField}
            defaultValue="50"
            variant="outlined"
            size="small"
            onChange={maxRunsOnChange}
          />
        </Tooltip>

        <Button
          variant="contained"
          color="primary"
          data-testid="flaky-tests-search-button"
          onClick={search}
          className={classes.searchButton}
        >
          Search
        </Button>
      </div>

      <LoadingSection
        loadingState={loadingState}
        successComponent={
          <RepositoryFlakyTestsDetails
            flakyTests={flakyTests}
            repoName={repoName}
            hideIfEmpty={hideIfEmpty}
          />
        }
      />
    </div>
  );
};

export default RepositoryFlakyTestsPage;
