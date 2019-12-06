import * as React from "react";
import { makeStyles, TableCell, TableRow } from "@material-ui/core";

const useStyles = makeStyles(theme => ({
  resultCol: {
    width: "10px",
    marginRight: "8px",
    marginLeft: "8px"
  }
}));

interface TestCaseListHeaderRowProps {
  showDurationFirst: boolean;
}

const TestCaseListHeaderRow = ({
  showDurationFirst
}: TestCaseListHeaderRowProps) => {
  const classes = useStyles({});

  let headerCells = [];
  const resultHeaderCell = (
    <TableCell
      className={classes.resultCol}
      key="result-header"
      role="rowheader"
      data-testid="test-list-result-header"
    >
      Result
    </TableCell>
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

  return <TableRow>{headerCells.map(headerCell => headerCell)}</TableRow>;
};

export default TestCaseListHeaderRow;
