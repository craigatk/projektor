import * as React from "react";
import { ListItem, ListItemText } from "@mui/material";
import styled from "styled-components";

interface DashboardSummaryItemProps {
  label: string;
  testId: string;
  value: any;
}

const ItemLabel = styled.span`
  min-width: 105px;
  display: inline-block;
`

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
            <ItemLabel>{label}</ItemLabel>
            <span data-testid={testId}>{value}</span>
          </span>
        }
      />
    </ListItem>
  );
};

export default DashboardSummaryItem;
