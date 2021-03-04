import * as React from "react";
import { RouteComponentProps } from "@reach/router";
import LoadingState from "../../Loading/LoadingState";
import PageTitle from "../../PageTitle";
import LoadingSection from "../../Loading/LoadingSection";
import AdminFailuresDetails from "./AdminFailuresDetails";
import { ResultsProcessingFailure } from "../../model/AdminModel";
import { fetchRecentFailures } from "../../service/AdminService";

interface AdminFailuresPageProps extends RouteComponentProps {}

const AdminFailuresPage = ({}: AdminFailuresPageProps) => {
  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading
  );
  const [failures, setFailures] = React.useState<ResultsProcessingFailure[]>(
    []
  );

  React.useEffect(() => {
    fetchRecentFailures(10)
      .then((response) => {
        setFailures(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  }, [setFailures, setLoadingState]);

  return (
    <div>
      <PageTitle title="Recent errors" testid="admin-failures-title" />

      <LoadingSection
        loadingState={loadingState}
        successComponent={<AdminFailuresDetails failures={failures} />}
      />
    </div>
  );
};

export default AdminFailuresPage;
