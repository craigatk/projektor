import * as React from "react";
import { render } from "react-dom";
import { Router } from "@reach/router";
import Welcome from "./Welcome";
import TestRunDataWrapper from "./TestRun/TestRunDataWrapper";
import OrganizationWrapper from "./Organization/OrganizationWrapper";
import RepositoryWrapper from "./Repository/RepositoryWrapper";

const App = () => {
  return (
    <Router>
      <Welcome path="/" />
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
      <TestRunDataWrapper path="/tests/:publicId/*" publicId="" />
    </Router>
  );
};

render(<App />, document.getElementById("root"));
