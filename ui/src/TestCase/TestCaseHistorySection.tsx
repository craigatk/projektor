import * as React from "react";
import { useEffect } from "react";
import { RouteComponentProps } from "@reach/router";
import moment from "moment-timezone";
import {
  Box,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Tooltip as MuiTooltip,
  Typography,
} from "@mui/material";
import {
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import classes from "./TestCaseHistorySection.module.css";
import { TestCaseHistory, TestCaseHistoryEntry } from "../model/TestRunModel";
import { fetchTestCaseHistory } from "../service/TestRunService";
import LoadingState from "../Loading/LoadingState";
import LoadingSection from "../Loading/LoadingSection";
import CleanLink from "../Link/CleanLink";
import PassedIcon from "../Icons/PassedIcon";
import FailedIcon from "../Icons/FailedIcon";
import SkippedIcon from "../Icons/SkippedIcon";
import { formatSecondsDuration } from "../dateUtils/dateUtils";

interface TestCaseHistorySectionProps extends RouteComponentProps {
  publicId: string;
  testSuiteIdx: number;
  testCaseIdx: number;
}

const testCaseLink = (entry: TestCaseHistoryEntry) =>
  `/tests/${entry.publicId}/suite/${entry.testSuiteIdx}/case/${entry.testCaseIdx}`;

const formatDate = (timestamp: Date) =>
  moment(timestamp).format("MMM Do YYYY, h:mm a");

const resultText = (entry: TestCaseHistoryEntry) =>
  entry.skipped ? "skipped" : entry.passed ? "passed" : "failed";

const resultColor = (entry: TestCaseHistoryEntry) =>
  entry.skipped ? "grey.400" : entry.passed ? "success.main" : "error.main";

const ResultIcon = ({ entry }: { entry: TestCaseHistoryEntry }) =>
  entry.skipped ? (
    <SkippedIcon className={classes.resultIcon} />
  ) : entry.passed ? (
    <PassedIcon className={classes.resultIcon} />
  ) : (
    <FailedIcon className={classes.resultIcon} />
  );

const CommitText = ({ entry }: { entry: TestCaseHistoryEntry }) =>
  entry.commitSha ? (
    <code>{entry.commitSha.substring(0, 7)}</code>
  ) : (
    <span>unknown commit</span>
  );

const FailureStreakCallout = ({ history }: { history: TestCaseHistory }) => {
  const { firstFailure, lastPassedBeforeFailure } = history;

  if (!firstFailure) {
    return null;
  }

  return (
    <div
      className={classes.callout}
      data-testid="test-case-history-failure-streak"
    >
      <Typography variant="body1">
        Failing since{" "}
        <CleanLink
          to={testCaseLink(firstFailure)}
          data-testid="test-case-history-first-failure-link"
        >
          {formatDate(firstFailure.createdTimestamp)}
        </CleanLink>{" "}
        at <CommitText entry={firstFailure} />
      </Typography>
      <Typography variant="body1">
        {lastPassedBeforeFailure ? (
          <>
            Last passed{" "}
            <CleanLink
              to={testCaseLink(lastPassedBeforeFailure)}
              data-testid="test-case-history-last-passed-link"
            >
              {formatDate(lastPassedBeforeFailure.createdTimestamp)}
            </CleanLink>{" "}
            at <CommitText entry={lastPassedBeforeFailure} />
          </>
        ) : (
          <span data-testid="test-case-history-no-pass">
            No passing run in the available history
          </span>
        )}
      </Typography>
    </div>
  );
};

const ResultStrip = ({
  entries,
  publicId,
}: {
  entries: TestCaseHistoryEntry[];
  publicId: string;
}) => (
  <div>
    <Typography variant="subtitle2">Results, oldest to newest</Typography>
    <div className={classes.strip} data-testid="test-case-history-strip">
      {[...entries].reverse().map((entry) => (
        <MuiTooltip
          key={entry.publicId}
          title={`${resultText(entry)} · ${formatDate(entry.createdTimestamp)}`}
        >
          <CleanLink
            to={testCaseLink(entry)}
            aria-label={`${resultText(entry)} ${formatDate(entry.createdTimestamp)}`}
            data-testid={`test-case-history-strip-${entry.publicId}`}
          >
            <Box
              className={classes.stripCell}
              sx={{
                backgroundColor: resultColor(entry),
                outline:
                  entry.publicId === publicId ? "2px solid #1c313a" : "none",
              }}
            />
          </CleanLink>
        </MuiTooltip>
      ))}
    </div>
  </div>
);

const DurationTooltip = (props) => {
  if (props.payload && props.payload.length >= 1) {
    const { createdTimestamp, duration, result } = props.payload[0].payload;
    return (
      <div className={classes.tooltip}>
        <div>{formatSecondsDuration(duration)}</div>
        <div>{result}</div>
        <div>{formatDate(createdTimestamp)}</div>
      </div>
    );
  }
  return null;
};

const DurationChart = ({ entries }: { entries: TestCaseHistoryEntry[] }) => {
  const data = [...entries]
    .reverse()
    .filter((entry) => !entry.skipped && entry.duration != null)
    .map((entry) => ({
      createdTimestamp: entry.createdTimestamp,
      duration: entry.duration,
      result: resultText(entry),
    }));

  return (
    <div data-testid="test-case-history-duration-chart">
      <Typography variant="subtitle2">Duration</Typography>
      <ResponsiveContainer width="100%" height={200}>
        <LineChart data={data} margin={{ top: 10, right: 30, left: 10 }}>
          <CartesianGrid strokeDasharray="3 3" vertical={false} />
          <XAxis
            dataKey="createdTimestamp"
            tickFormatter={(value) => moment(value).format("MMM Do")}
          />
          <YAxis tickFormatter={(value) => formatSecondsDuration(value, 3)} />
          <Tooltip content={<DurationTooltip />} />
          <Line
            type="monotone"
            dataKey="duration"
            stroke="#8884d8"
            strokeWidth={2}
            isAnimationActive={false}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
};

const HistoryTable = ({
  entries,
  publicId,
}: {
  entries: TestCaseHistoryEntry[];
  publicId: string;
}) => (
  <Table size="small" data-testid="test-case-history-table">
    <TableHead>
      <TableRow>
        <TableCell>Run</TableCell>
        <TableCell>Result</TableCell>
        <TableCell>Duration</TableCell>
        <TableCell>Branch</TableCell>
        <TableCell>Commit</TableCell>
      </TableRow>
    </TableHead>
    <TableBody>
      {entries.map((entry, idx) => (
        <TableRow
          key={entry.publicId}
          selected={entry.publicId === publicId}
          data-testid={`test-case-history-row-${idx + 1}`}
        >
          <TableCell>
            <CleanLink to={testCaseLink(entry)}>
              {formatDate(entry.createdTimestamp)}
            </CleanLink>
          </TableCell>
          <TableCell data-testid={`test-case-history-row-${idx + 1}-result`}>
            <ResultIcon entry={entry} /> {resultText(entry)}
          </TableCell>
          <TableCell>
            {entry.duration != null
              ? formatSecondsDuration(entry.duration)
              : ""}
          </TableCell>
          <TableCell>
            {entry.branchName}
            {entry.pullRequestNumber ? ` (PR #${entry.pullRequestNumber})` : ""}
          </TableCell>
          <TableCell>
            {entry.commitSha ? (
              <code>{entry.commitSha.substring(0, 7)}</code>
            ) : (
              ""
            )}
          </TableCell>
        </TableRow>
      ))}
    </TableBody>
  </Table>
);

const TestCaseHistorySection = ({
  publicId,
  testSuiteIdx,
  testCaseIdx,
}: TestCaseHistorySectionProps) => {
  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading,
  );
  const [history, setHistory] = React.useState<TestCaseHistory>(null);

  useEffect(() => {
    fetchTestCaseHistory(publicId, testSuiteIdx, testCaseIdx)
      .then((response) => {
        setHistory(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  }, [publicId, testSuiteIdx, testCaseIdx]);

  return (
    <LoadingSection
      loadingState={loadingState}
      successComponent={
        history?.entries?.length > 0 ? (
          <div
            data-testid="test-case-history-section"
            className={classes.section}
          >
            <FailureStreakCallout history={history} />
            <ResultStrip entries={history.entries} publicId={publicId} />
            <DurationChart entries={history.entries} />
            <HistoryTable entries={history.entries} publicId={publicId} />
          </div>
        ) : (
          <Typography data-testid="test-case-history-empty">
            No history available. Test history is built from CI runs published
            with git metadata.
          </Typography>
        )
      }
    />
  );
};

export default TestCaseHistorySection;
