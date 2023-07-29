import * as React from "react";
import { TestSuite } from "../model/TestRunModel";
import {
  Location,
  Link,
  Router,
  Redirect,
  LocationContext,
} from "@reach/router";
import { Tabs, Tab, Paper } from "@mui/material";
import TestSuiteTestCaseList from "./TestSuiteTestCaseList";
import TestSuiteSystemOut from "../TestOutput/TestSuiteSystemOut";
import TestSuiteSystemErr from "../TestOutput/TestSuiteSystemErr";
import { getTabCurrentValue } from "../Tabs/TabValue";
import BreadcrumbPageHeader from "../BreadcrumbPageHeader";
import CleanLink from "../Link/CleanLink";
import styled from "styled-components";

interface TestSuiteDetailsProps {
  publicId: string;
  testSuite: TestSuite;
}

const DetailsWrapper = styled(Paper)`
  padding: 16px 16px;
`

const DetailsSection = styled.div`
  padding-top: 20px;
`

const buildHeaderIntermediateLinks = (publicId, testSuite) => {
  const headerIntermediateLinks = [];

  if (testSuite.packageName != null && testSuite.packageName !== "") {
    headerIntermediateLinks.push(
      <CleanLink
        to={`/tests/${publicId}/suites/package?name=${testSuite.packageName}`}
        data-testid={`breadcrumb-link-package-name`}
        key="package-name-link"
      >
        {testSuite.packageName}
      </CleanLink>,
    );
  }

  return headerIntermediateLinks;
};

const TestSuiteDetails = ({ publicId, testSuite }: TestSuiteDetailsProps) => {
  const linkBase = `/tests/${publicId}/suite/${testSuite.idx}`;

  const defaultTab = "/cases";

  const headerIntermediateLinks = buildHeaderIntermediateLinks(
    publicId,
    testSuite,
  );

  return (
    <div data-testid="test-suite-details">
      <BreadcrumbPageHeader
        intermediateLinks={headerIntermediateLinks}
        endingText={testSuite.className}
      />
      <DetailsWrapper elevation={1}>
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

        <DetailsSection>
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
        </DetailsSection>
      </DetailsWrapper>
    </div>
  );
};

export default TestSuiteDetails;
