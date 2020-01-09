import * as React from "react";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import { TestSuite } from "../model/TestRunModel";
import CleanLink from "../Link/CleanLink";

interface TestSuiteListProps {
  publicId: String;
  testSuites: TestSuite[];
}

const TestSuiteList = ({ publicId, testSuites }: TestSuiteListProps) => {
  return (
    <Table size="small" data-testid="test-suite-list">
      <TableHead>
        <TableRow>
          <TableCell>Test Suite</TableCell>
          <TableCell>Total tests</TableCell>
          <TableCell>Passing tests</TableCell>
          <TableCell>Failed tests</TableCell>
          <TableCell>Skipped tests</TableCell>
          <TableCell>Duration (sec)</TableCell>
        </TableRow>
      </TableHead>
      <TableBody>
        {testSuites.map(testSuite => (
          <TableRow key={`test-stuite-${testSuite.idx}`}>
            <TableCell
              data-testid={`test-suite-class-name-${testSuite.idx}`}
              size="small"
            >
              <CleanLink to={`/tests/${publicId}/suite/${testSuite.idx}/`}>
                {testSuite.packageName != null
                  ? `${testSuite.packageName}.`
                  : null}
                {testSuite.className}
              </CleanLink>
            </TableCell>
            <TableCell size="small">{testSuite.testCount}</TableCell>
            <TableCell size="small">{testSuite.passingCount}</TableCell>
            <TableCell size="small">{testSuite.failureCount}</TableCell>
            <TableCell size="small">{testSuite.skippedCount}</TableCell>
            <TableCell size="small">{testSuite.duration}</TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
};

export default TestSuiteList;
