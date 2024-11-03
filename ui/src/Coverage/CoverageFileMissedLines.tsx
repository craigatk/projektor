import * as React from "react";
import GitHubFileLink from "../VersionControl/GitHubFileLink";
import { Typography } from "@mui/material";
import { TestRunGitMetadata } from "../model/TestRunModel";
import CleanLinkText from "../Link/CleanLinkText";

interface CoverageFileMissedLinesProps {
  missedLines: number[];
  filePath: string;
  fileIdx: number;
  gitMetadata?: TestRunGitMetadata;
}

const CoverageFileMissedLines = ({
  missedLines,
  filePath,
  fileIdx,
  gitMetadata,
}: CoverageFileMissedLinesProps) => {
  const [showFull, setShowFull] = React.useState(missedLines.length <= 10);

  const missedLinesToShow = showFull ? missedLines : missedLines.slice(0, 10);

  const onClick = () => {
    setShowFull(true);
  };

  return (
    <Typography
      data-testid={`coverage-file-missed-lines-${fileIdx}`}
      variant="caption"
    >
      {missedLinesToShow.map((missedLine, lineIdx) => (
        <span key={`coverage-file-${fileIdx}-missed-line-link-${lineIdx}`}>
          <GitHubFileLink
            gitMetadata={gitMetadata}
            filePath={filePath}
            linkText={missedLine.toString()}
            lineNumber={missedLine}
            testId={`coverage-file-${fileIdx}-missed-line-link-${missedLine}`}
          />
          {lineIdx < missedLinesToShow.length - 1 && <span>, </span>}
        </span>
      ))}
      {!showFull && (
        <span>
          {" "}
          ...{" "}
          <CleanLinkText
            onClick={onClick}
            data-testid={`coverage-file-${fileIdx}-show-all-missed-lines-link`}
          >
            show all
          </CleanLinkText>
        </span>
      )}
    </Typography>
  );
};

export default CoverageFileMissedLines;
