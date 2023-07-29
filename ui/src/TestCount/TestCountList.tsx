import * as React from "react";
import { List, ListItem, ListItemIcon, ListItemText } from "@mui/material";
import PassedIcon from "../Icons/PassedIcon";
import FailedIcon from "../Icons/FailedIcon";
import TotalIcon from "../Icons/TotalIcon";
import SkippedIcon from "../Icons/SkippedIcon";
import styled, { css } from "styled-components";

interface TestCountListProps {
  passedCount: number;
  failedCount: number;
  skippedCount: number;
  totalCount: number;
  horizontal: boolean;
}

const CountList = styled(List)<{ horizontal: boolean }>`
  ${(props) =>
    props.horizontal &&
    css`
      display: flex;
      flex-direction: row;
      padding: 0;
    `}
`;

const TestCountList = ({
  passedCount,
  failedCount,
  skippedCount,
  totalCount,
  horizontal,
}: TestCountListProps) => {
  return (
    <CountList dense={true} horizontal={horizontal}>
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
    </CountList>
  );
};

export default TestCountList;
