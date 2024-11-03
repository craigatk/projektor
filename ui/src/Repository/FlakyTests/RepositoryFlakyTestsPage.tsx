import * as React from "react";
import classes from "./RepositoryFlakyTestsPage.module.css";
import LoadingState from "../../Loading/LoadingState";
import { RouteComponentProps } from "@reach/router";
import { RepositoryFlakyTests } from "../../model/RepositoryModel";
import { fetchRepositoryFlakyTests } from "../../service/RepositoryService";
import LoadingSection from "../../Loading/LoadingSection";
import RepositoryFlakyTestsDetails from "./RepositoryFlakyTestsDetails";
import {
  Button,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  TextField,
  Tooltip,
} from "@mui/material";
import PageTitle from "../../PageTitle";
import { NumberParam, StringParam, useQueryParam } from "use-query-params";

interface RepositoryFlakyTestsPageProps extends RouteComponentProps {
  orgPart: string;
  repoPart: string;
  projectName?: string;
  hideIfEmpty: boolean;
}

const RepositoryFlakyTestsPage = ({
  orgPart,
  repoPart,
  projectName,
  hideIfEmpty,
}: RepositoryFlakyTestsPageProps) => {
  const repoName = `${orgPart}/${repoPart}`;

  const [flakyTests, setFlakyTests] =
    React.useState<RepositoryFlakyTests>(null);
  const [maxRuns, setMaxRuns] = useQueryParam("max", NumberParam);
  const [flakyThreshold, setFlakyThreshold] = useQueryParam(
    "threshold",
    NumberParam,
  );
  const [branchType, setBranchType] = useQueryParam("branch_type", StringParam);
  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading,
  );

  const doFetch = () => {
    fetchRepositoryFlakyTests(
      maxRuns || 50,
      flakyThreshold || 5,
      branchType || "mainline",
      repoName,
      projectName,
    )
      .then((response) => {
        setFlakyTests(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  };

  React.useEffect(doFetch, [setFlakyTests, setLoadingState]);

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

  const handleBranchTypeChange = (e) => {
    setBranchType(e.target.value);
  };

  const search = () => {
    setLoadingState(LoadingState.Loading);
    doFetch();
  };

  return (
    <div>
      <PageTitle title="Flaky tests" testid="repository-flaky-tests-title" />

      <div className={classes.paramSection}>
        <Tooltip
          title="How many times a test has to fail to be considered flaky"
          placement="top"
        >
          <span>
            <TextField
              label="Flaky test threshold"
              data-testid="flaky-tests-threshold"
              className={classes.textField}
              defaultValue={flakyThreshold || 5}
              variant="outlined"
              size="small"
              onChange={flakyThresholdOnChange}
            />
          </span>
        </Tooltip>

        <Tooltip
          title="How many test runs to look back and examine when looking for flaky tests"
          placement="top"
        >
          <TextField
            label="How many tests back"
            data-testid="flaky-tests-max-runs"
            className={classes.textField}
            defaultValue={maxRuns || 50}
            variant="outlined"
            size="small"
            onChange={maxRunsOnChange}
          />
        </Tooltip>

        <FormControl className={classes.selectControl} variant="outlined">
          <InputLabel className={classes.selectLabel}>
            In which branches
          </InputLabel>
          <Select
            data-testid="flaky-tests-branch-type"
            value={branchType || "mainline"}
            onChange={handleBranchTypeChange}
            SelectDisplayProps={{ className: classes.selectOption }}
          >
            <MenuItem
              value="mainline"
              data-testid="flaky-tests-branch-type-mainline"
            >
              Mainline only
            </MenuItem>
            <MenuItem value="all" data-testid="flaky-tests-branch-type-all">
              All branches
            </MenuItem>
          </Select>
        </FormControl>

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
