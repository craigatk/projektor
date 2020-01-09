import * as React from "react";
import { makeStyles } from "@material-ui/core/styles";
import { TestSuite } from "../model/TestRunModel";
import {
  Location,
  Link,
  Router,
  Redirect,
  LocationContext
} from "@reach/router";
import { Tabs, Tab, Paper } from "@material-ui/core";
import TestSuiteTestCaseList from "./TestSuiteTestCaseList";
import TestSuiteSystemOut from "../TestOutput/TestSuiteSystemOut";
import TestSuiteSystemErr from "../TestOutput/TestSuiteSystemErr";
import { getTabCurrentValue } from "../Tabs/TabValue";
import BreadcrumbPageHeader from "../BreadcrumbPageHeader";
import CleanLink from "../Link/CleanLink";

interface TestSuiteDetailsProps {
  publicId: string;
  testSuite: TestSuite;
}

const useStyles = makeStyles(theme => ({
  detailsSection: {
    paddingTop: "20px"
  },
  paper: {
    padding: theme.spacing(2, 2)
  }
}));

const buildHeaderIntermediateLinks = (publicId, testSuite) => {
  const headerIntermediateLinks = [];

  if (testSuite.packageName != null && testSuite.packageName !== "") {
    headerIntermediateLinks.push(
      <CleanLink
        to={`/tests/${publicId}/suites/package/${testSuite.packageName}`}
        data-testid={`breadcrumb-link-package-name`}
        key="package-name-link"
      >
        {testSuite.packageName}
      </CleanLink>
    );
  }

  return headerIntermediateLinks;
};

const TestSuiteDetails = ({ publicId, testSuite }: TestSuiteDetailsProps) => {
  const linkBase = `/tests/${publicId}/suite/${testSuite.idx}`;

  const classes = useStyles({});

  const defaultTab = "/cases";

  const headerIntermediateLinks = buildHeaderIntermediateLinks(
    publicId,
    testSuite
  );

  return (
    <div data-testid="test-suite-details">
      <BreadcrumbPageHeader
        intermediateLinks={headerIntermediateLinks}
        endingText={testSuite.className}
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
                label="Test cases"
                value="/cases"
                data-testid="test-suite-tab-test-case-list"
                component={Link}
                to={`${linkBase}/cases`}
              />
              {testSuite.hasSystemOut && (
                <Tab
                  label="System out"
                  value="/systemOut"
                  data-testid="test-suite-tab-system-out"
                  component={Link}
                  to={`${linkBase}/systemOut`}
                />
              )}
              {testSuite.hasSystemErr && (
                <Tab
                  label="System err"
                  value="/systemErr"
                  data-testid="test-suite-tab-system-err"
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
            <TestSuiteTestCaseList
              path="/cases"
              publicId={publicId}
              testSuite={testSuite}
            />
            <TestSuiteSystemOut
              path="/systemOut"
              publicId={publicId}
              testSuiteIdx={testSuite.idx}
            />
            <TestSuiteSystemErr
              path="/systemErr"
              publicId={publicId}
              testSuiteIdx={testSuite.idx}
            />
          </Router>
        </div>
      </Paper>
    </div>
  );
};

export default TestSuiteDetails;
