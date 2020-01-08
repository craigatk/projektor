import * as React from "react";
import { TableCell, TableRow } from "@material-ui/core";
import { TestCase } from "../../model/TestRunModel";
import TestCaseResultIcon from "../TestCaseResultIcon";
import { Link } from "@reach/router";
import { fullTestCaseName } from "../../model/TestCaseHelpers";

interface TestCaseListRowProps {
  publicId: string;
  testCase: TestCase;
  showDurationFirst: boolean;
  showFullTestCaseName: boolean;
}

const TestCaseListRow = ({
  publicId,
  testCase,
  showDurationFirst,
  showFullTestCaseName
}: TestCaseListRowProps) => {
  let rowCells = [];
  const resultRowCell = (
    <TableCell
      key="test-result-cell"
      role="rowcell"
      data-testid={`test-case-result-${testCase.testSuiteIdx}-${testCase.idx}`}
      size="small"
    >
      <TestCaseResultIcon testCase={testCase} />
    </TableCell>
  );
  const testRowCell = (
    <TableCell
      data-testid={`test-case-name-${testCase.testSuiteIdx}-${testCase.idx}`}
      key="test-name-cell"
      role="rowcell"
      size="small"
    >
      <Link
        to={`/tests/${publicId}/suite/${testCase.testSuiteIdx}/case/${testCase.idx}/`}
        data-testid={`test-case-name-link-${testCase.testSuiteIdx}-${testCase.idx}`}
      >
        {showFullTestCaseName ? fullTestCaseName(testCase) : testCase.name}
      </Link>
    </TableCell>
  );
  const durationRowCell = (
    <TableCell
      key="test-duration-cell"
      role="rowcell"
      data-testid={`test-case-duration-${testCase.testSuiteIdx}-${testCase.idx}`}
      size="small"
    >
      {testCase.duration}s
    </TableCell>
  );

  if (showDurationFirst) {
    rowCells = [durationRowCell, testRowCell, resultRowCell];
  } else {
    rowCells = [resultRowCell, testRowCell, durationRowCell];
  }

  return <TableRow>{rowCells.map(rowCell => rowCell)}</TableRow>;
};

export default TestCaseListRow;
