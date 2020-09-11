import * as React from "react";
import { OrganizationCoverage } from "../../model/OrganizationModel";
import LoadingState from "../../Loading/LoadingState";
import { fetchOrganizationCoverage } from "../../service/OrganizationService";
import LoadingSection from "../../Loading/LoadingSection";
import OrganizationCoverageDetails from "./OrganizationCoverageDetails";
import { RouteComponentProps } from "@reach/router";

interface OrganizationCoveragePageProps extends RouteComponentProps {
  orgName: string;
}

const OrganizationCoveragePage = ({
  orgName,
}: OrganizationCoveragePageProps) => {
  const [organizationCoverage, setOrganizationCoverage] = React.useState<
    OrganizationCoverage
  >(null);
  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading
  );

  React.useEffect(() => {
    fetchOrganizationCoverage(orgName)
      .then((response) => {
        setOrganizationCoverage(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  }, [setOrganizationCoverage]);

  return (
    <LoadingSection
      loadingState={loadingState}
      successComponent={
        <OrganizationCoverageDetails
          organizationCoverage={organizationCoverage}
        />
      }
    />
  );
};

export default OrganizationCoveragePage;
