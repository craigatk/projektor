import * as React from "react";
import { RepositoryFlakyTests } from "../../model/RepositoryModel";
import MaterialTable from "@material-table/core";
import CleanLink from "../../Link/CleanLink";
import moment from "moment-timezone";

interface FlakyTestsTableProps {
  flakyTests: RepositoryFlakyTests;
}

const headerStyle = {
  paddingTop: "8px",
  paddingBottom: "8px",
};

const cellStyle = {
  padding: "6px 24px 6px 16px",
};

const RepositoryFlakyTestsTable = ({ flakyTests }: FlakyTestsTableProps) => {
  const sortedFlakyTests = flakyTests.tests.sort(
    (a, b) => b.failureCount - a.failureCount
  );

  return (
    <div data-testid="repository-flaky-tests-table">
      <MaterialTable
        title=""
        style={{ boxShadow: "none" }}
        options={{
          sorting: true,
          paging: false,
        }}
        columns={[
          {
            title: "Test case name",
            field: "name",
            render: (rowData) => (
              <CleanLink
                to={`/tests/${rowData.latestTestCase.publicId}/suite/${rowData.testSuiteIdx}/case/${rowData.testCaseIdx}`}
                data-testid={`flaky-test-case-name-${rowData.idx}`}
              >
                {rowData.name}
              </CleanLink>
            ),
            cellStyle,
            headerStyle,
          },
          {
            title: "Failure percentage",
            field: "failurePercentage",
            render: (rowData) => (
              <span
                data-testid={`flaky-test-case-failure-percentage-${rowData.idx}`}
              >
                {rowData.failurePercentage}%
              </span>
            ),
            cellStyle,
            headerStyle,
          },
          {
            title: "Failure count",
            field: "failureCount",
            render: (rowData) => (
              <span
                data-testid={`flaky-test-case-failure-count-${rowData.idx}`}
              >
                {rowData.failureCount}
              </span>
            ),
            cellStyle,
            headerStyle,
          },
          {
            title: "First failure",
            field: "firstCreatedTimestamp",
            render: (rowData) => (
              <CleanLink
                to={`/tests/${rowData.firstTestCase.publicId}/suite/${rowData.firstTestCase.testSuiteIdx}/case/${rowData.firstTestCase.idx}`}
                data-testid={`flaky-test-case-first-failure-${rowData.idx}`}
              >
                {moment(rowData.firstCreatedTimestamp).format(
                  "MMM Do YYYY, h:mm a"
                )}
              </CleanLink>
            ),
            cellStyle,
            headerStyle,
          },
          {
            title: "Latest failure",
            field: "latestCreatedTimestamp",
            render: (rowData) => (
              <CleanLink
                to={`/tests/${rowData.latestTestCase.publicId}/suite/${rowData.latestTestCase.testSuiteIdx}/case/${rowData.latestTestCase.idx}`}
                data-testid={`flaky-test-case-latest-failure-${rowData.idx}`}
              >
                {moment(rowData.latestCreatedTimestamp).format(
                  "MMM Do YYYY, h:mm a"
                )}
              </CleanLink>
            ),
            cellStyle,
            headerStyle,
          },
        ]}
        data={sortedFlakyTests.map((flakyTest, idx) => ({
          idx: idx + 1,
          name: flakyTest.testCase.fullName,
          testCaseIdx: flakyTest.testCase.idx,
          testSuiteIdx: flakyTest.testCase.testSuiteIdx,
          failureCount: flakyTest.failureCount,
          failurePercentage: flakyTest.failurePercentage,
          firstTestCase: flakyTest.firstTestCase,
          firstCreatedTimestamp: flakyTest.firstTestCase.createdTimestamp,
          latestTestCase: flakyTest.latestTestCase,
          latestCreatedTimestamp: flakyTest.latestTestCase.createdTimestamp,
        }))}
      />
    </div>
  );
};

export default RepositoryFlakyTestsTable;
