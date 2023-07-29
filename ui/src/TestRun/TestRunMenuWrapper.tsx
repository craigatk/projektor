import * as React from "react";
import { globalHistory, Router } from "@reach/router";
import { QueryParamProvider } from "use-query-params";
import TestRunAllTests from "./TestRunAllTests";
import TestSuitePage from "../TestSuite/TestSuitePage";
import TestSuitePackagePage from "../TestSuite/TestSuitePackagePage";
import TestCasePage from "../TestCase/TestCasePage";
import FailedTestCases from "../TestCase/FailedTestCases";
import Dashboard from "../Dashboard/Dashboard";
import { TestRunGitMetadata, TestRunSummary } from "../model/TestRunModel";
import SideMenu from "../SideMenu/SideMenu";
import SlowTestCasesPage from "../TestCase/slow/SlowTestCasesPage";
import { AppBar, Typography } from "@mui/material";
import AttachmentsPage from "../Attachments/AttachmentsPage";
import { PinState } from "../Pin/PinState";
import CoveragePage from "../Coverage/CoveragePage";
import CoverageGroupFilesPage from "../Coverage/CoverageGroupFilesPage";
import CodeQualityReportsPage from "../Quality/CodeQualityReportsPage";
import styled from "styled-components";

const Root = styled.div`
  display: flex;
`

const TestRunAppBar = styled(AppBar)`
  background-color: #1c313a;
  padding: 5px 10px;
  height: 42px;
`

const AppBarLabel = styled(Typography)`
  margin-left: 192px;
`

const TestRunContent = styled.main`
  flex-grow: 1;
  margin-top: 42px;
  max-width: calc(100% - 180px);
`

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

  return (
    <Root data-testid="test-run-menu-wrapper">
      <PinState publicId={publicId}>
        <TestRunAppBar>
          {gitMetadata && gitMetadata.repoName && (
            <AppBarLabel variant="subtitle1">
              {gitMetadata.repoName}
            </AppBarLabel>
          )}
        </TestRunAppBar>
        <SideMenu
          publicId={publicId}
          testRunSummary={testRunSummary}
          hasAttachments={hasAttachments}
          hasCoverage={hasCoverage}
          gitMetadata={gitMetadata}
        />
        <TestRunContent>
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
                path="/suites/package"
                publicId={publicId}
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
              <CodeQualityReportsPage path="/quality/*" publicId={publicId} />
            </Router>
          </QueryParamProvider>
        </TestRunContent>
      </PinState>
    </Root>
  );
};

export default TestRunMenuWrapper;
