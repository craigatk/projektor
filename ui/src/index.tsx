import * as React from "react";
import { render } from "react-dom";
import { Router } from "@reach/router";
import Welcome from "./Welcome";
import TestRunDataWrapper from "./TestRun/TestRunDataWrapper";

const App = () => {
  return (
    <Router>
      <Welcome path="/" />
      <TestRunDataWrapper path="/tests/:publicId/*" publicId="" />
    </Router>
  );
};

render(<App />, document.getElementById("root"));
