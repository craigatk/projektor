import * as React from "react";
import classes from "./DashboardSummaryItem.module.css";
import { ListItem, ListItemText } from "@mui/material";

interface DashboardSummaryItemProps {
  label: string;
  testId: string;
  value: any;
}

const DashboardSummaryItem = ({
  label,
  testId,
  value,
}: DashboardSummaryItemProps) => {
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

export default DashboardSummaryItem;
