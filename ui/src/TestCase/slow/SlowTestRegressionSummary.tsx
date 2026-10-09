import * as React from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import classes from "./SlowTestRegressionSummary.module.css";
import listClasses from "../list/TestCaseListHeaderRow.module.css";
import sectionClasses from "./SlowTestCasesSection.module.css";
import { SlowTestRegressions } from "../../model/TestRunModel";
import { fullTestCaseName } from "../../model/TestCaseHelpers";
import { createTestCaseIdentifier } from "../testCaseHelpers";
import CleanLink from "../../Link/CleanLink";
import { formatSecondsDuration } from "../../dateUtils/dateUtils";

interface SlowTestRegressionSummaryProps {
  publicId: string;
  slowTestRegressions: SlowTestRegressions;
}

const pluralize = (count: number, singular: string, plural: string) =>
  `${count} ${count === 1 ? singular : plural}`;

const SlowTestRegressionSummary = ({
  publicId,
  slowTestRegressions,
}: SlowTestRegressionSummaryProps) => {
  const { regressions, thresholdPercent, baselineRunCount } =
    slowTestRegressions;

  return (
    <section className={classes.section} data-testid="slow-test-regressions">
      <Typography
        component="h2"
        variant="h6"
        className={sectionClasses.sectionTitle}
        data-testid="slow-test-regressions-title"
      >
        Slower than baseline
      </Typography>
      <Typography
        variant="subtitle1"
        className={classes.headline}
        data-testid="slow-test-regressions-headline"
      >
        {pluralize(regressions.length, "test", "tests")} more than{" "}
        {thresholdPercent}% slower than their baseline
      </Typography>
      <Typography variant="body2" className={classes.description}>
        Baseline is each test's median duration across its passing runs in the
        previous {pluralize(baselineRunCount, "CI run", "CI runs")} on this
        branch.
      </Typography>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell className={listClasses.durationFirstCol}>
              Duration
            </TableCell>
            <TableCell>Test</TableCell>
            <TableCell className={classes.numeric}>Baseline</TableCell>
            <TableCell className={classes.numeric}>Change</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {regressions.map(
            ({
              testCase,
              baselineDuration,
              durationIncrease,
              increasePercent,
            }) => {
              const identifier = createTestCaseIdentifier(testCase);
              return (
                <TableRow
                  key={identifier}
                  data-testid={`slow-test-regression-${identifier}`}
                >
                  <TableCell>
                    {formatSecondsDuration(testCase.duration)}
                  </TableCell>
                  <TableCell>
                    <CleanLink
                      to={`/tests/${publicId}/suite/${testCase.testSuiteIdx}/case/${testCase.idx}/`}
                      data-testid={`slow-test-regression-link-${identifier}`}
                    >
                      {fullTestCaseName(testCase)}
                    </CleanLink>
                  </TableCell>
                  <TableCell className={classes.numeric}>
                    {formatSecondsDuration(baselineDuration)}
                  </TableCell>
                  <TableCell
                    className={`${classes.numeric} ${classes.change}`}
                    data-testid={`slow-test-regression-change-${identifier}`}
                  >
                    +{formatSecondsDuration(durationIncrease)} (+
                    {Math.round(increasePercent)}%)
                  </TableCell>
                </TableRow>
              );
            },
          )}
        </TableBody>
      </Table>
    </section>
  );
};

export default SlowTestRegressionSummary;
