import * as React from "react";
import classes from "./TestRunDuration.module.css";
import { List, ListItem, ListItemText } from "@mui/material";
import CleanLink from "../Link/CleanLink";
import { formatSecondsDuration } from "../dateUtils/dateUtils";

interface TestRunDurationProps {
  publicId: string;
  averageDuration: number;
  cumulativeDuration: number;
  wallClockDuration?: number;
  slowestTestCaseDuration: number;
}

const TestRunDuration = ({
  publicId,
  averageDuration,
  cumulativeDuration,
  wallClockDuration,
  slowestTestCaseDuration,
}: TestRunDurationProps) => {
  return (
    <List dense={true}>
      <ListItem>
        <ListItemText
          primary={
            <span>
              <span className={classes.label}>Average duration</span>
              <span data-testid="test-run-average-duration">
                {averageDuration}s
              </span>
            </span>
          }
        />
      </ListItem>
      {wallClockDuration && (
        <ListItem>
          <ListItemText
            primary={
              <span>
                <span className={classes.label}>Wall clock duration</span>
                <span data-testid="test-run-wall-clock-duration">
                  {formatSecondsDuration(wallClockDuration)}
                </span>
              </span>
            }
          />
        </ListItem>
      )}
      <ListItem>
        <ListItemText
          primary={
            <span>
              <span className={classes.label}>Cumulative duration</span>
              <span data-testid="test-run-cumulative-duration">
                {formatSecondsDuration(cumulativeDuration)}
              </span>
            </span>
          }
        />
      </ListItem>
      <ListItem>
        <ListItemText
          primary={
            <span>
              <CleanLink
                to={`/tests/${publicId}/slow`}
                className={classes.label}
                data-testid="test-run-slow-test-cases-link"
              >
                Slowest test case
              </CleanLink>
              <span data-testid="test-run-slowest-test-case-duration">
                {formatSecondsDuration(slowestTestCaseDuration)}
              </span>
            </span>
          }
        />
      </ListItem>
    </List>
  );
};

export default TestRunDuration;
