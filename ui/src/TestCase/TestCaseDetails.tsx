import * as React from "react";
import {
  Link,
  Location,
  LocationContext,
  Redirect,
  Router,
} from "@reach/router";
import classes from "./TestCaseDetails.module.css";
import { AttachmentType, TestCase } from "../model/TestRunModel";
import BreadcrumbPageHeader from "../BreadcrumbPageHeader";
import Paper from "@mui/material/Paper";
import TestCaseFailureDetails from "./TestCaseFailureDetails";
import { Tab, Tabs } from "@mui/material";
import TestSuiteSystemOut from "../TestOutput/TestSuiteSystemOut";
import TestSuiteSystemErr from "../TestOutput/TestSuiteSystemErr";
import { getTabCurrentValue } from "../Tabs/TabValue";
import TestCaseSummary from "./TestCaseSummary";
import CleanLink from "../Link/CleanLink";
import TestCaseSystemOut from "../TestOutput/TestCaseSystemOut";
import TestCaseSystemErr from "../TestOutput/TestCaseSystemErr";
import TestCaseFailureVideo from "./TestCaseFailureVideo";
import { findAttachmentOfType } from "./testCaseHelpers";
import TestCaseFailureScreenshot from "./TestCaseFailureScreenshot";

interface TestCaseDetailsProps {
  publicId: string;
  testCase: TestCase;
}

const buildHeaderIntermediateLinks = (
  publicId: string,
  testCase: TestCase,
): React.ReactNode[] => {
  const headerIntermediateLinks = [];

  if (testCase.packageName != null && testCase.packageName !== "") {
    headerIntermediateLinks.push(
      <CleanLink
        to={`/tests/${publicId}/suites/package?name=${testCase.packageName}`}
        data-testid={`breadcrumb-link-package-name`}
        key="package-name-link"
      >
        {testCase.packageName}
      </CleanLink>,
    );
  }

  if (testCase.className != null && testCase.className !== "") {
    headerIntermediateLinks.push(
      <CleanLink
        to={`/tests/${publicId}/suite/${testCase.testSuiteIdx}/`}
        data-testid={`breadcrumb-link-class-name`}
        key="class-name-link"
      >
        {testCase.className}
      </CleanLink>,
    );
  }
  return headerIntermediateLinks;
};

const TestCaseDetails = ({ publicId, testCase }: TestCaseDetailsProps) => {
  const linkBase = `/tests/${publicId}/suite/${testCase.testSuiteIdx}/case/${testCase.idx}`;

  const defaultTab =
    testCase.passed || testCase.skipped ? "/summary" : "/failure";

  const headerIntermediateLinks = buildHeaderIntermediateLinks(
    publicId,
    testCase,
  );

  const hasScreenshotAttachment = !!findAttachmentOfType(
    testCase,
    AttachmentType.IMAGE,
  );
  const hasVideoAttachment = !!findAttachmentOfType(
    testCase,
    AttachmentType.VIDEO,
  );

  return (
    <div data-testid="test-case-details">
      <BreadcrumbPageHeader
        intermediateLinks={headerIntermediateLinks}
        endingText={testCase.name}
      />
      <Paper elevation={1} className={classes.paper}>
        <Location>
          {({ location }: LocationContext) => (
            <Tabs
              value={getTabCurrentValue(location, defaultTab)}
              indicatorColor="primary"
              textColor="primary"
            >
              <Tab
                label="Summary"
                value="/summary"
                data-testid="test-case-tab-summary"
                component={Link}
                to={`${linkBase}/summary`}
              />
              {!testCase.passed && !testCase.skipped ? (
                <Tab
                  label="Failure details"
                  value="/failure"
                  data-testid="test-case-tab-failure"
                  component={Link}
                  to={`${linkBase}/failure`}
                />
              ) : null}
              {testCase.hasSystemOut && (
                <Tab
                  label="System out"
                  value="/systemOut"
                  data-testid="test-case-tab-system-out"
                  component={Link}
                  to={`${linkBase}/systemOut`}
                />
              )}
              {testCase.hasSystemErr && (
                <Tab
                  value="/systemErr"
                  label="System err"
                  data-testid="test-case-tab-system-err"
                  component={Link}
                  to={`${linkBase}/systemErr`}
                />
              )}
              {hasScreenshotAttachment && (
                <Tab
                  value="/screenshot"
                  label="Screenshot"
                  data-testid="test-case-tab-screenshot"
                  component={Link}
                  to={`${linkBase}/screenshot`}
                />
              )}
              {hasVideoAttachment && (
                <Tab
                  value="/video"
                  label="Video"
                  data-testid="test-case-tab-video"
                  component={Link}
                  to={`${linkBase}/video`}
                />
              )}
            </Tabs>
          )}
        </Location>

        <div className={classes.detailsSection}>
          <Router>
            <Redirect from="/" to={`${linkBase}${defaultTab}`} noThrow={true} />
            <TestCaseSummary path="/summary" testCase={testCase} />
            <TestCaseFailureDetails
              path="/failure"
              failure={testCase.failure}
            />
            {testCase.hasSystemOutTestCase ? (
              <TestCaseSystemOut
                path="/systemOut"
                publicId={publicId}
                testSuiteIdx={testCase.testSuiteIdx}
                testCaseIdx={testCase.idx}
                data-testid="test-case-system-out"
              />
            ) : (
              <TestSuiteSystemOut
                path="/systemOut"
                publicId={publicId}
                testSuiteIdx={testCase.testSuiteIdx}
                data-testid="test-case-system-out"
              />
            )}
            {testCase.hasSystemErrTestCase ? (
              <TestCaseSystemErr
                path="/systemErr"
                publicId={publicId}
                testSuiteIdx={testCase.testSuiteIdx}
                testCaseIdx={testCase.idx}
                data-testid="test-case-system-err"
              />
            ) : (
              <TestSuiteSystemErr
                path="/systemErr"
                publicId={publicId}
                testSuiteIdx={testCase.testSuiteIdx}
                data-testid="test-case-system-err"
              />
            )}
            <TestCaseFailureScreenshot
              path="/screenshot"
              testCase={testCase}
              publicId={publicId}
            />
            <TestCaseFailureVideo
              path="/video"
              testCase={testCase}
              publicId={publicId}
            />
          </Router>
        </div>
      </Paper>
    </div>
  );
};

export default TestCaseDetails;
