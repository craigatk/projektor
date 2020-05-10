import * as React from "react";
import CodeTextLine from "./CodeTextLine";
import { Element } from "react-scroll";
import { makeStyles } from "@material-ui/core/styles";

interface CodeTextProgressiveRenderProps {
  lines: string[];
  isLineHighlighted: (index: number) => boolean;
  handleLineClick: (event: React.MouseEvent, clickedIdx: number) => void;
  highlightedLine: number;
  highlightedRangeEnd: number;
  renderComplete: () => void;
  pageSize: number;
}

interface CodeTextProgressiveRenderStyleProps {
  lineCount: number;
  lineHeight: number;
}

const useStyles = makeStyles({
  // style rule
  wrapper: ({
    lineCount,
    lineHeight,
  }: CodeTextProgressiveRenderStyleProps) => ({
    height: lineCount * lineHeight,
    display: "inline-block",
    width: "100%",
  }),
});

const CodeTextProgressiveRender = ({
  lines,
  highlightedLine,
  highlightedRangeEnd,
  isLineHighlighted,
  handleLineClick,
  renderComplete,
  pageSize,
}: CodeTextProgressiveRenderProps) => {
  const classes = useStyles({ lineCount: lines.length, lineHeight: 13.333 });

  const [currentRenderLimit, setCurrentRenderLimit] = React.useState(pageSize);
  const maxRenderSize = lines.length - 1;

  setTimeout(() => {
    if (currentRenderLimit < maxRenderSize) {
      setCurrentRenderLimit(currentRenderLimit + pageSize);
    } else {
      renderComplete();
    }
  });

  const allLines = lines.map((line, idx) => {
    const lineIdx = idx + 1;

    return React.useMemo(
      () => (
        <Element name={`line-${lineIdx}`} key={`line-element-${lineIdx}`}>
          <CodeTextLine
            key={`code-line-${lineIdx}`}
            line={line}
            idx={lineIdx}
            highlighted={isLineHighlighted(lineIdx)}
            handleLineClick={handleLineClick}
          />
        </Element>
      ),
      [line, lineIdx, highlightedLine, highlightedRangeEnd]
    );
  });

  return (
    <div className={classes.wrapper}>
      {allLines.slice(0, currentRenderLimit)}
    </div>
  );
};

export default CodeTextProgressiveRender;
