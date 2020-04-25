import * as React from "react";
import { ListItem, ListItemText, makeStyles } from "@material-ui/core";

interface TestRunDateItemProps {
  label: string;
  testId: string;
  value: any;
}

const useStyles = makeStyles({
  label: {
    minWidth: "105px",
    display: "inline-block",
  },
});

const TestRunDateItem = ({ label, testId, value }: TestRunDateItemProps) => {
  const classes = useStyles({});
  return (
    <ListItem>
      <ListItemText
        primary={
          <span>
            <span className={classes.label}>{label}</span>
            <span data-testid={testId}>{value}</span>
          </span>
        }
      />
    </ListItem>
  );
};

export default TestRunDateItem;
