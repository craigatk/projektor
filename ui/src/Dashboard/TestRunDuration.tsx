import * as React from "react";
import { List, ListItem, ListItemText, makeStyles } from "@material-ui/core";
import CleanLink from "../Link/CleanLink";

interface TestRunDurationProps {
  publicId: string;
  averageDuration: number;
  cumulativeDuration: number;
  slowestTestCaseDuration: number;
}

const useStyles = makeStyles({
  label: {
    minWidth: "135px",
    display: "inline-block",
  },
});

const TestRunDuration = ({
  publicId,
  averageDuration,
  cumulativeDuration,
  slowestTestCaseDuration,
}: TestRunDurationProps) => {
  const classes = useStyles({});
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
      <ListItem>
        <ListItemText
          primary={
            <span>
              <span className={classes.label}>Cumulative duration</span>
              <span data-testid="test-run-cumulative-duration">
                {cumulativeDuration}s
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
                {slowestTestCaseDuration}s
              </span>
            </span>
          }
        />
      </ListItem>
    </List>
  );
};

export default TestRunDuration;
