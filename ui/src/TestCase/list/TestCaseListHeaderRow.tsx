import * as React from "react";
import { TableCell, TableRow } from "@mui/material";
import styled from "styled-components";

const ResultsColumn = styled(TableCell)`
  width: 10px;
  margin-right: 8px;
  margin-left: 8px;
`

interface TestCaseListHeaderRowProps {
  showDurationFirst: boolean;
}

const TestCaseListHeaderRow = ({
  showDurationFirst,
}: TestCaseListHeaderRowProps) => {
  let headerCells = [];
  const resultHeaderCell = (
    <ResultsColumn
      key="result-header"
      role="rowheader"
      data-testid="test-list-result-header"
    >
      Result
    </ResultsColumn>
  );
  const testHeaderCell = (
    <TableCell
      key="test-header"
      role="rowheader"
      data-testid="test-list-name-header"
    >
      Test
    </TableCell>
  );
  const durationHeaderCell = (
    <TableCell
      key="duration-header"
      role="rowheader"
      data-testid="test-list-duration-header"
    >
      Duration
    </TableCell>
  );

  if (showDurationFirst) {
    headerCells = [durationHeaderCell, testHeaderCell, resultHeaderCell];
  } else {
    headerCells = [resultHeaderCell, testHeaderCell, durationHeaderCell];
  }

  return <TableRow>{headerCells.map((headerCell) => headerCell)}</TableRow>;
};

export default TestCaseListHeaderRow;
