import * as React from "react";
import classes from "./RepositoryCoverageSearch.module.css";
import LoadingState from "../../Loading/LoadingState";
import { fetchRepositoryCoverageTimeline } from "../../service/RepositoryService";
import LoadingSection from "../../Loading/LoadingSection";
import { RouteComponentProps } from "@reach/router";
import { RepositoryCoverageTimeline } from "../../model/RepositoryModel";
import RepositoryCoverageDetails from "./RepositoryCoverageDetails";
import {
  Button,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
} from "@mui/material";
import PageTitle from "../../PageTitle";
import { StringParam, useQueryParam } from "use-query-params";

interface RepositoryCoverageSearchProps extends RouteComponentProps {
  repoName: string;
  projectName?: string;
  hideIfEmpty?: boolean;
}

const RepositoryCoverageSearch = ({
  repoName,
  projectName,
  hideIfEmpty,
}: RepositoryCoverageSearchProps) => {
  const [repositoryCoverageTimeline, setRepositoryCoverageTimeline] =
    React.useState<RepositoryCoverageTimeline>(null);
  const [branchType, setBranchType] = useQueryParam("branch", StringParam);
  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading,
  );

  const doFetch = () => {
    fetchRepositoryCoverageTimeline(repoName, projectName, branchType)
      .then((response) => {
        setRepositoryCoverageTimeline(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  };

  React.useEffect(doFetch, [setRepositoryCoverageTimeline, setLoadingState]);

  const handleBranchTypeChange = (e) => {
    const newBranchValue = e.target.value;
    setBranchType(newBranchValue);
  };

  const search = () => {
    setLoadingState(LoadingState.Loading);
    doFetch();
  };

  return (
    <div>
      <PageTitle
        title="Coverage over time"
        testid="repository-coverage-title"
      />

      <div className={classes.paramSection}>
        <FormControl className={classes.selectControl} variant="outlined">
          <InputLabel className={classes.selectLabel}>
            Coverage from which branches
          </InputLabel>
          <Select
            data-testid="repository-coverage-branch-type"
            value={branchType || "mainline"}
            onChange={handleBranchTypeChange}
            SelectDisplayProps={{ className: classes.selectOption }}
            className={classes.selectBox}
          >
            <MenuItem
              value="mainline"
              data-testid="repository-coverage-branch-type-mainline"
            >
              Mainline only
            </MenuItem>
            <MenuItem
              value="all"
              data-testid="repository-coverage-branch-type-all"
            >
              All branches
            </MenuItem>
          </Select>
        </FormControl>

        <Button
          variant="contained"
          color="primary"
          data-testid="repository-coverage-search-button"
          onClick={search}
          className={classes.searchButton}
        >
          Search
        </Button>
      </div>

      <LoadingSection
        loadingState={loadingState}
        successComponent={
          <RepositoryCoverageDetails
            coverageTimeline={repositoryCoverageTimeline}
            repoName={repoName}
            projectName={projectName}
            hideIfEmpty={hideIfEmpty}
          />
        }
      />
    </div>
  );
};

export default RepositoryCoverageSearch;
