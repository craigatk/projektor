import * as React from "react";
import { RepositoryFlakyTests } from "../../model/RepositoryModel";
import MaterialTable from "material-table";
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
                to={`/tests/${rowData.latestPublicId}/suite/${rowData.testSuiteIdx}/case/${rowData.testCaseIdx}`}
                data-testid={`flaky-test-case-name-${rowData.idx}`}
              >
                {rowData.name}
              </CleanLink>
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
            title: "Latest failure",
            field: "latestCreatedTimestamp",
            render: (rowData) => (
              <CleanLink
                to={`/tests/${rowData.latestPublicId}/suite/${rowData.testSuiteIdx}/case/${rowData.testCaseIdx}`}
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
          latestPublicId: flakyTest.latestPublicId,
          latestCreatedTimestamp: flakyTest.latestCreatedTimestamp,
        }))}
      />
    </div>
  );
};

export default RepositoryFlakyTestsTable;
