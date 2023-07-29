import * as React from "react";
import { List, ListItem, ListItemText } from "@mui/material";
import CleanLink from "../Link/CleanLink";
import { formatSecondsDuration } from "../dateUtils/dateUtils";
import styled from "styled-components";

interface TestRunDurationProps {
  publicId: string;
  averageDuration: number;
  cumulativeDuration: number;
  wallClockDuration?: number;
  slowestTestCaseDuration: number;
}

const ItemLabel = styled.span`
  min-width: 135px;
  display: inline-block;
`

const ItemLink = styled(CleanLink)`
  min-width: 135px;
  display: inline-block;
`

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
              <ItemLabel>Average duration</ItemLabel>
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
                <ItemLabel>Wall clock duration</ItemLabel>
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
              <ItemLabel>Cumulative duration</ItemLabel>
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
              <ItemLink
                to={`/tests/${publicId}/slow`}
                data-testid="test-run-slow-test-cases-link"
              >
                Slowest test case
              </ItemLink>
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
