import * as React from "react";
import {
  Link,
  Router,
  Location,
  Redirect,
  LocationContext
} from "@reach/router";
import { makeStyles } from "@material-ui/core/styles";
import { TestCase } from "../model/TestRunModel";
import BreadcrumbPageHeader from "../BreadcrumbPageHeader";
import Paper from "@material-ui/core/Paper";
import TestCaseFailureDetails from "./TestCaseFailureDetails";
import { Tabs, Tab } from "@material-ui/core";
import TestSuiteSystemOut from "../TestOutput/TestSuiteSystemOut";
import TestSuiteSystemErr from "../TestOutput/TestSuiteSystemErr";
import { getTabCurrentValue } from "../Tabs/TabValue";
import TestCaseSummary from "./TestCaseSummary";

const useStyles = makeStyles(theme => ({
  paper: {
    padding: theme.spacing(1, 2)
  },
  detailsSection: {
    paddingTop: "20px"
  }
}));

interface TestCaseDetailsProps {
  publicId: string;
  testCase: TestCase;
}

const buildHeaderIntermediateLinks = (
  publicId: string,
  testCase: TestCase
): React.ReactNode[] => {
  const headerIntermediateLinks = [];

  if (testCase.packageName != null && testCase.packageName !== "") {
    headerIntermediateLinks.push(
      <Link
        to={`/tests/${publicId}/suites/package/${testCase.packageName}`}
        data-testid={`breadcrumb-link-package-name`}
        key="package-name-link"
      >
        {testCase.packageName}
      </Link>
    );
  }

  if (testCase.className != null && testCase.className !== "") {
    headerIntermediateLinks.push(
      <Link
        to={`/tests/${publicId}/suite/${testCase.testSuiteIdx}/`}
        data-testid={`breadcrumb-link-class-name`}
        key="class-name-link"
      >
        {testCase.className}
      </Link>
    );
  }
  return headerIntermediateLinks;
};

const TestCaseDetails = ({ publicId, testCase }: TestCaseDetailsProps) => {
  const classes = useStyles({});

  const linkBase = `/tests/${publicId}/suite/${testCase.testSuiteIdx}/case/${testCase.idx}`;

  const defaultTab =
    testCase.passed || testCase.skipped ? "/summary" : "/failure";

  const headerIntermediateLinks = buildHeaderIntermediateLinks(
    publicId,
    testCase
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
            <TestSuiteSystemOut
              path="/systemOut"
              publicId={publicId}
              testSuiteIdx={testCase.testSuiteIdx}
              data-testid="test-case-system-out"
            />
            <TestSuiteSystemErr
              path="/systemErr"
              publicId={publicId}
              testSuiteIdx={testCase.testSuiteIdx}
              data-testid="test-case-system-err"
            />
          </Router>
        </div>
      </Paper>
    </div>
  );
};

export default TestCaseDetails;
