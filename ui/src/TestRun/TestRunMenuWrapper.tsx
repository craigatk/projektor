import * as React from "react";
import { globalHistory, Router } from "@reach/router";
import { QueryParamProvider } from "use-query-params";
import { makeStyles } from "@material-ui/styles";
import TestRunAllTests from "./TestRunAllTests";
import TestSuitePage from "../TestSuite/TestSuitePage";
import TestSuitePackagePage from "../TestSuite/TestSuitePackagePage";
import TestCasePage from "../TestCase/TestCasePage";
import FailedTestCases from "../TestCase/FailedTestCases";
import Dashboard from "../Dashboard/Dashboard";
import { TestRunSummary } from "../model/TestRunModel";
import TestRunSideMenu from "./TestRunSideMenu";
import SlowTestCasesPage from "../TestCase/slow/SlowTestCasesPage";
import { AppBar, Typography } from "@material-ui/core";

const useStyles = makeStyles(theme => ({
  root: {
    display: "flex"
  },
  appBar: {
    backgroundColor: "#1c313a",
    padding: "5px 10px"
  },
  content: {
    flexGrow: 1,
    marginTop: "42px"
  }
}));

interface TestRunMenuWrapperProps {
  publicId: string;
  testRunSummary: TestRunSummary;
}

const TestRunMenuWrapper = ({
  publicId,
  testRunSummary
}: TestRunMenuWrapperProps) => {
  if (testRunSummary == null) {
    return null;
  }

  const classes = useStyles({});

  return (
    <div className={classes.root} data-testid="test-run-menu-wrapper">
      <AppBar className={classes.appBar}>
        <Typography variant="h6">Projektor</Typography>
      </AppBar>
      <TestRunSideMenu publicId={publicId} testRunSummary={testRunSummary} />
      <main className={classes.content}>
        <QueryParamProvider reachHistory={globalHistory}>
          <Router>
            <Dashboard
              path="/"
              publicId={publicId}
              testRunSummary={testRunSummary}
            />
            <TestRunAllTests path="/all" publicId={publicId} />
            <FailedTestCases path="/failed" publicId={publicId} />
            <SlowTestCasesPage path="/slow" publicId={publicId} />
            <TestSuitePage
              path="/suite/:testSuiteIdx/*"
              publicId={publicId}
              testSuiteIdx={0}
            />
            <TestSuitePackagePage
              path="/suites/package/:packageName"
              publicId={publicId}
              packageName="default"
            />
            <TestCasePage
              path="/suite/:testSuiteIdx/case/:testCaseIdx/*"
              publicId={publicId}
              testSuiteIdx={0}
              testCaseIdx={0}
            />
          </Router>
        </QueryParamProvider>
      </main>
    </div>
  );
};

export default TestRunMenuWrapper;
