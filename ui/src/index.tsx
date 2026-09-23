import * as React from "react";
import { createRoot } from "react-dom/client";
import { RouteComponentProps, Router } from "@reach/router";
import Welcome from "./Welcome";
import TestRunDataWrapper from "./TestRun/TestRunDataWrapper";
import OrganizationWrapper from "./Organization/OrganizationWrapper";
import RepositoryWrapper from "./Repository/RepositoryWrapper";
import AdminWrapper from "./Admin/AdminWrapper";

// Keyed by public ID so navigating from one test run to another
// (such as from a test case's history) remounts and reloads the test run data
const TestRunRoute = ({
  publicId,
}: RouteComponentProps<{ publicId: string }>) => (
  <TestRunDataWrapper key={publicId} publicId={publicId} />
);

const App = () => {
  return (
    <Router>
      <Welcome path="/" />
      <AdminWrapper path="/admin/*" />
      <OrganizationWrapper path="/organization/:orgName/*" orgName="" />
      <RepositoryWrapper
        path="/repository/:orgPart/:repoPart/project/:projectName/*"
        orgPart=""
        repoPart=""
      />
      <RepositoryWrapper
        path="/repository/:orgPart/:repoPart/*"
        orgPart=""
        repoPart=""
      />
      <TestRunRoute path="/tests/:publicId/*" />
    </Router>
  );
};

const root = createRoot(document.getElementById("root")!);
root.render(<App />);
