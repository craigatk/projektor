import * as React from "react";
import moment from "moment";
import { List, ListItem, ListItemText, makeStyles } from "@material-ui/core";

interface TestRunDateProps {
  createdTimestamp: Date;
}

const useStyles = makeStyles({
  label: {
    minWidth: "105px",
    display: "inline-block"
  }
});

const TestRunDate = ({ createdTimestamp }: TestRunDateProps) => {
  const classes = useStyles({});
  return (
    <List dense={true}>
      <ListItem>
        <ListItemText
          primary={
            <span>
              <span className={classes.label}>Report created</span>
              <span data-testid="test-run-report-created-timestamp">
                {moment(createdTimestamp).format("MMMM Do YYYY, h:mm:ss a")}
              </span>
            </span>
          }
        />
      </ListItem>
    </List>
  );
};

export default TestRunDate;
