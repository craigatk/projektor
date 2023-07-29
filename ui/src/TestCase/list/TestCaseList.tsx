import * as React from "react";
import { TestCase } from "../../model/TestRunModel";
import { Table, TableHead, TableBody } from "@mui/material";
import TestCaseListRow from "./TestCaseListRow";
import TestCaseListHeaderRow from "./TestCaseListHeaderRow";

interface TestCaseListProps {
  publicId: string;
  testCases: TestCase[];
  showFullTestCaseName: boolean;
  showDurationFirst?: boolean;
}

const TestCaseList = ({
  publicId,
  testCases,
  showFullTestCaseName,
  showDurationFirst,
}: TestCaseListProps) => {
  return (
    <Table size="small">
      <TableHead>
        <TestCaseListHeaderRow showDurationFirst={showDurationFirst} />
      </TableHead>
      <TableBody>
        {testCases.map((testCase: TestCase) => (
          <TestCaseListRow
            key={`test-case-${testCase.testSuiteIdx}-${testCase.idx}`}
            publicId={publicId}
            testCase={testCase}
            showDurationFirst={showDurationFirst}
            showFullTestCaseName={showFullTestCaseName}
          />
        ))}
      </TableBody>
    </Table>
  );
};

export default TestCaseList;
