import * as React from "react";
import classes from "./TestCountList.module.css";
import classNames from "classnames/bind";
import { List, ListItem, ListItemIcon, ListItemText } from "@mui/material";
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

const cx = classNames.bind(classes);

const TestCountList = ({
  passedCount,
  failedCount,
  skippedCount,
  totalCount,
  horizontal,
}: TestCountListProps) => {
  return (
    <List
      dense={true}
      className={cx({
        horizontalList: horizontal,
      })}
    >
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
