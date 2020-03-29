import * as React from "react";
import { TestSuite } from "../model/TestRunModel";
import CleanLink from "../Link/CleanLink";
import {
  anyTestSuiteHasGroupName,
  fullTestSuiteName,
} from "../model/TestSuiteHelpers";
import MaterialTable from "material-table";

interface TestSuiteListProps {
  publicId: String;
  testSuites: TestSuite[];
}

const headerStyle = {
  paddingTop: "8px",
  paddingBottom: "8px",
};

const cellStyle = {
  padding: "6px 24px 6px 16px",
};

const TestSuiteList = ({ publicId, testSuites }: TestSuiteListProps) => {
  return (
    <div data-testid="test-suite-list">
      <MaterialTable
        title=""
        style={{ boxShadow: "none" }}
        options={{
          sorting: true,
          paging: false,
        }}
        columns={[
          {
            title: "Test Suite",
            field: "name",
            render: (rowData) => (
              <CleanLink
                to={`/tests/${publicId}/suite/${rowData.idx}/`}
                data-testid={`test-suite-class-name-${rowData.idx}`}
              >
                {rowData.name}
              </CleanLink>
            ),
            cellStyle,
            headerStyle,
          },
          {
            title: "Group",
            field: "group",
            hidden: !anyTestSuiteHasGroupName(testSuites),
            render: (rowData) => (
              <span data-testid={`test-suite-group-name-${rowData.idx}`}>
                {rowData.group}
              </span>
            ),
            cellStyle,
            headerStyle,
          },
          { title: "Passed", field: "passed", cellStyle, headerStyle },
          { title: "Failed", field: "failed", cellStyle, headerStyle },
          { title: "Duration", field: "duration", cellStyle, headerStyle },
        ]}
        data={testSuites.map((testSuite) => ({
          name: fullTestSuiteName(testSuite),
          group: testSuite.groupName || "",
          passed: testSuite.passingCount,
          failed: testSuite.failureCount,
          duration: `${testSuite.duration}s`,
          idx: testSuite.idx,
        }))}
      />
    </div>
  );
};

export default TestSuiteList;
