import * as React from "react";
import classes from "./CodeTextLine.module.css";
import LinkOutlinedIcon from "@mui/icons-material/LinkOutlined";

interface CodeTextLineProps {
  line: string;
  idx: number;
  highlighted: boolean;
  handleLineClick(e: React.MouseEvent, lineIdx: number): void;
}

const CodeTextLine = ({
  line,
  idx,
  handleLineClick,
  highlighted,
}: CodeTextLineProps) => {
  function handleClick(e: React.MouseEvent) {
    handleLineClick(e, idx);
  }

  return (
    <div
      className={classes.line}
      data-testid={`code-text-line-${idx}-${highlighted}`}
      style={{ backgroundColor: highlighted ? "#F9F9F9" : "inherit" }}
    >
      <span
        className={classes.lineNumber}
        data-testid={`code-text-line-number-${idx}`}
        onClick={handleClick}
      >
        <LinkOutlinedIcon className={classes.linkIcon} fontSize="small" />
        <span className={classes.lineNumberValue}>{idx}</span>
      </span>
      <span data-testid={`code-text-line-content-${idx}`}>{line}</span>
    </div>
  );
};

function linesAreEqual(
  prevLine: CodeTextLineProps,
  nextLine: CodeTextLineProps,
) {
  return (
    prevLine.idx === nextLine.idx &&
    prevLine.line === nextLine.line &&
    prevLine.highlighted === nextLine.highlighted
  );
}

export default React.memo(CodeTextLine, linesAreEqual);
