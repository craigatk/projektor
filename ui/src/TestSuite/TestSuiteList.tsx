import * as React from "react";
import { TestSuite } from "../model/TestRunModel";
import CleanLink from "../Link/CleanLink";
import {
  anyTestSuiteHasGroupName,
  fullTestSuiteName
} from "../model/TestSuiteHelpers";
import MaterialTable from "material-table";

interface TestSuiteListProps {
  publicId: String;
  testSuites: TestSuite[];
}

const TestSuiteList = ({ publicId, testSuites }: TestSuiteListProps) => {
  return (
    <div data-testid="test-suite-list">
      <MaterialTable
        title=""
        style={{ boxShadow: "none" }}
        options={{
          sorting: true,
          paging: false
        }}
        columns={[
          {
            title: "Test Suite",
            field: "name",
            render: rowData => (
              <CleanLink
                to={`/tests/${publicId}/suite/${rowData.idx}/`}
                data-testid={`test-suite-class-name-${rowData.idx}`}
              >
                {rowData.name}
              </CleanLink>
            )
          },
          {
            title: "Group",
            field: "group",
            hidden: !anyTestSuiteHasGroupName(testSuites),
            render: rowData => (
              <span data-testid={`test-suite-group-name-${rowData.idx}`}>
                {rowData.group}
              </span>
            )
          },
          { title: "Passed", field: "passed" },
          { title: "Failed", field: "failed" },
          { title: "Duration", field: "duration" }
        ]}
        data={testSuites.map(testSuite => ({
          name: fullTestSuiteName(testSuite),
          group: testSuite.groupName || "",
          passed: testSuite.passingCount,
          failed: testSuite.failureCount,
          duration: `${testSuite.duration}s`,
          idx: testSuite.idx
        }))}
      />
    </div>
  );
};

export default TestSuiteList;
