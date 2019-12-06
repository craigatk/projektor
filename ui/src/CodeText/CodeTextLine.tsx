import * as React from "react";
import { makeStyles } from "@material-ui/core/styles";

interface CodeTextLineProps {
  line: String;
  idx: number;
  highlighted: boolean;
  handleLineClick(e: React.MouseEvent, lineIdx: number): void;
}

interface CodeTextLineStyleProps {
  highlighted: boolean;
}

const useStyles = makeStyles({
  // style rule
  lineClass: ({ highlighted }: CodeTextLineStyleProps) => ({
    backgroundColor: highlighted ? "#F9F9F9" : "inherit",
    cursor: "default",
    "&:hover": {
      backgroundColor: "lightgrey"
    },
    fontSize: ".9em"
  }),
  lineNumberClass: {
    userSelect: "none",
    minWidth: "40px",
    display: "inline-block",
    textAlign: "right",
    paddingRight: "15px"
  }
});

const CodeTextLine = ({
  line,
  idx,
  handleLineClick,
  highlighted
}: CodeTextLineProps) => {
  const classes = useStyles({ highlighted });

  function handleClick(e: React.MouseEvent) {
    handleLineClick(e, idx);
  }

  return (
    <div
      onClick={handleClick}
      className={classes.lineClass}
      data-testid={`code-text-line-${idx}`}
    >
      <span
        className={classes.lineNumberClass}
        data-testid={`code-text-line-number-${idx}`}
      >
        {idx}
      </span>
      <span data-testid={`code-text-line-content-${idx}`}>{line}</span>
    </div>
  );
};

export default CodeTextLine;
