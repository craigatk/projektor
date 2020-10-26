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
import { TestRunGitMetadata, TestRunSummary } from "../model/TestRunModel";
import SideMenu from "../SideMenu/SideMenu";
import SlowTestCasesPage from "../TestCase/slow/SlowTestCasesPage";
import { AppBar, Typography } from "@material-ui/core";
import AttachmentsPage from "../Attachments/AttachmentsPage";
import { PinState } from "../Pin/PinState";
import CoveragePage from "../Coverage/CoveragePage";
import CoverageGroupFilesPage from "../Coverage/CoverageGroupFilesPage";

const useStyles = makeStyles((theme) => ({
  root: {
    display: "flex",
  },
  appBar: {
    backgroundColor: "#1c313a",
    padding: "5px 10px",
    height: "42px",
  },
  appBarLabel: {
    marginLeft: "192px",
  },
  content: {
    flexGrow: 1,
    marginTop: "42px",
    maxWidth: "calc(100% - 180px)",
  },
}));

interface TestRunMenuWrapperProps {
  publicId: string;
  testRunSummary: TestRunSummary;
  hasAttachments: boolean;
  hasCoverage: boolean;
  gitMetadata?: TestRunGitMetadata;
}

const TestRunMenuWrapper = ({
  publicId,
  testRunSummary,
  hasAttachments,
  hasCoverage,
  gitMetadata,
}: TestRunMenuWrapperProps) => {
  if (testRunSummary == null) {
    return null;
  }

  const classes = useStyles({});

  return (
    <div className={classes.root} data-testid="test-run-menu-wrapper">
      <PinState publicId={publicId}>
        <AppBar className={classes.appBar}>
          {gitMetadata && gitMetadata.repoName && (
            <Typography variant="subtitle1" className={classes.appBarLabel}>
              {gitMetadata.repoName}
            </Typography>
          )}
        </AppBar>
        <SideMenu
          publicId={publicId}
          testRunSummary={testRunSummary}
          hasAttachments={hasAttachments}
          hasCoverage={hasCoverage}
          gitMetadata={gitMetadata}
        />
        <main className={classes.content}>
          <QueryParamProvider reachHistory={globalHistory}>
            <Router>
              <Dashboard
                path="/"
                publicId={publicId}
                testRunSummary={testRunSummary}
                gitMetadata={gitMetadata}
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
              <AttachmentsPage path="/attachments" publicId={publicId} />
              <CoverageGroupFilesPage
                path="/coverage/:coverageGroupName/files"
                publicId={publicId}
                coverageGroupName=""
              />
              <CoveragePage path="/coverage" publicId={publicId} />
            </Router>
          </QueryParamProvider>
        </main>
      </PinState>
    </div>
  );
};

export default TestRunMenuWrapper;
