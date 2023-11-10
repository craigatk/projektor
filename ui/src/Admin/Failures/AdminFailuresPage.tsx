import * as React from "react";
import { RouteComponentProps } from "@reach/router";
import LoadingState from "../../Loading/LoadingState";
import PageTitle from "../../PageTitle";
import LoadingSection from "../../Loading/LoadingSection";
import AdminFailuresDetails from "./AdminFailuresDetails";
import { ResultsProcessingFailure } from "../../model/AdminModel";
import { fetchRecentFailures } from "../../service/AdminService";
import { Button, TextField } from "@material-ui/core";
import { makeStyles } from "@material-ui/styles";

interface AdminFailuresPageProps extends RouteComponentProps {}

const useStyles = makeStyles(() => ({
  paramSection: {
    marginLeft: "10px",
    marginTop: "10px",
  },
  textField: {
    margin: "5px",
    width: "180px",
  },
  loadButton: {
    margin: "5px",
  },
}));

const AdminFailuresPage = ({}: AdminFailuresPageProps) => {
  const classes = useStyles({});

  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading,
  );
  const [failures, setFailures] = React.useState<ResultsProcessingFailure[]>(
    [],
  );
  const [fetchCount, setFetchCount] = React.useState<number>(10);

  const doFetch = () => {
    fetchRecentFailures(fetchCount)
      .then((response) => {
        setFailures(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  };

  React.useEffect(doFetch, [setFailures, setLoadingState]);

  const fetchCountOnChange = (e) => {
    const parsedValue = parseInt(e.target.value);

    if (parsedValue > 0) {
      setFetchCount(parsedValue);
    }
  };

  const load = () => {
    setLoadingState(LoadingState.Loading);
    doFetch();
  };

  return (
    <div>
      <PageTitle title="Recent errors" testid="admin-failures-title" />

      <div className={classes.paramSection}>
        <TextField
          label="Error count"
          data-testid="admin-failures-count-field"
          className={classes.textField}
          defaultValue={fetchCount}
          variant="outlined"
          size="small"
          onChange={fetchCountOnChange}
        />

        <Button
          variant="contained"
          color="primary"
          data-testid="admin-failures-load-button"
          onClick={load}
          className={classes.loadButton}
        >
          Load
        </Button>
      </div>

      <LoadingSection
        loadingState={loadingState}
        successComponent={<AdminFailuresDetails failures={failures} />}
      />
    </div>
  );
};

export default AdminFailuresPage;
