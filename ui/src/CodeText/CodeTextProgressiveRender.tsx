import * as React from "react";
import CodeTextLine from "./CodeTextLine";
import { Element } from "react-scroll";

interface CodeTextProgressiveRenderProps {
  lines: string[];
  isLineHighlighted: (index: number) => boolean;
  handleLineClick: (event: React.MouseEvent, clickedIdx: number) => void;
  highlightedLine: number;
  highlightedRangeEnd: number;
  renderComplete: () => void;
  pageSize: number;
}

const CodeTextProgressiveRender = ({
  lines,
  highlightedLine,
  highlightedRangeEnd,
  isLineHighlighted,
  handleLineClick,
  renderComplete,
  pageSize,
}: CodeTextProgressiveRenderProps) => {
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
        <CodeTextLine
          key={`code-line-${lineIdx}`}
          line={line}
          idx={lineIdx}
          highlighted={isLineHighlighted(lineIdx)}
          handleLineClick={handleLineClick}
        />
      ),
      [line, lineIdx, highlightedLine, highlightedRangeEnd]
    );
  });

  return (
    <span>
      {allLines.slice(0, currentRenderLimit).map((theLine, theLineIdx) => {
        return (
          <Element
            name={`line-${theLineIdx}`}
            key={`line-element-${theLineIdx}`}
          >
            {theLine}
          </Element>
        );
      })}
    </span>
  );
};

export default CodeTextProgressiveRender;
