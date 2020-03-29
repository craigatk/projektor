import * as React from "react";
import {
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  makeStyles,
} from "@material-ui/core";
import PassedIcon from "../Icons/PassedIcon";
import FailedIcon from "../Icons/FailedIcon";
import TotalIcon from "../Icons/TotalIcon";
import SkippedIcon from "../Icons/SkippedIcon";

interface TestCountListProps {
  passedCount: number;
  failedCount: number;
  skippedCount: number;
  totalCount: number;
  horizontal: boolean;
}

interface TestCountListStyleProps {
  horizontal: boolean;
}

const useStyles = makeStyles({
  // style rule
  list: ({ horizontal }: TestCountListStyleProps) =>
    horizontal
      ? {
          display: "flex",
          flexDirection: "row",
          padding: 0,
        }
      : {},
});

const TestCountList = ({
  passedCount,
  failedCount,
  skippedCount,
  totalCount,
  horizontal,
}: TestCountListProps) => {
  const classes = useStyles({ horizontal });

  return (
    <List dense={true} className={classes.list}>
      <ListItem>
        <ListItemIcon>
          <PassedIcon />
        </ListItemIcon>
        <ListItemText
          primary={`${passedCount} passed`}
          data-testid="test-count-list-passed"
        />
      </ListItem>
      <ListItem>
        <ListItemIcon>
          <FailedIcon />
        </ListItemIcon>
        <ListItemText
          primary={`${failedCount} failed`}
          data-testid="test-count-list-failed"
        />
      </ListItem>
      <ListItem>
        <ListItemIcon>
          <SkippedIcon />
        </ListItemIcon>
        <ListItemText
          primary={`${skippedCount} skipped`}
          data-testid="test-count-list-skipped"
        />
      </ListItem>
      <ListItem>
        <ListItemIcon>
          <TotalIcon />
        </ListItemIcon>
        <ListItemText
          primary={`${totalCount} total`}
          data-testid="test-count-list-total"
        />
      </ListItem>
    </List>
  );
};

export default TestCountList;
