import * as React from "react";
import { useQueryParam, NumberParam } from "use-query-params";
import CodeTextLine from "./CodeTextLine";
import { Element, scroller } from "react-scroll";

interface CodeTextProps {
  text: String;
}

const CodeText = ({ text }: CodeTextProps) => {
  if (text == null) {
    return null;
  }

  const [highlightedLine, setHighlightedLine] = useQueryParam("l", NumberParam);
  const [highlightedRangeEnd, setHighlightedRangeEnd] = useQueryParam(
    "le",
    NumberParam
  );

  const textLines = text.split(/\r?\n/g);
  const lastLine = textLines.pop();
  if (lastLine !== "") {
    textLines.push(lastLine);
  }

  function handleLineClick(event: React.MouseEvent, clickedIdx: number) {
    if (highlightedLine == null) {
      setHighlightedLine(clickedIdx);
    } else if (event.shiftKey && highlightedLine != null) {
      if (clickedIdx > highlightedLine) {
        setHighlightedRangeEnd(clickedIdx);
      } else {
        setHighlightedLine(clickedIdx);
        setHighlightedRangeEnd(highlightedLine);
      }
    } else {
      if (clickedIdx !== highlightedLine) {
        setHighlightedLine(clickedIdx);
      } else {
        setHighlightedLine(null);
      }
      setHighlightedRangeEnd(null);
    }
  }

  React.useEffect(() => {
    if (highlightedLine != null) {
      scroller.scrollTo(`line-${highlightedLine}`, {
        duration: 0,
        delay: 0,
        offset: -45,
        smooth: "easeInOutQuart",
      });
    }
    return () => {
      setHighlightedLine(null);
      setHighlightedRangeEnd(null);
    };
  }, [setHighlightedLine, scroller]);

  function isLineHighlighted(lineIdx: number) {
    if (highlightedRangeEnd != null) {
      return lineIdx >= highlightedLine && lineIdx <= highlightedRangeEnd;
    } else {
      return lineIdx === highlightedLine;
    }
  }

  return (
    <pre data-testid="code-text">
      {textLines.map((line, idx) => {
        const lineIdx = idx + 1;

        return (
          <Element name={`line-${lineIdx}`} key={`line-element-${lineIdx}`}>
            <CodeTextLine
              key={`code-line-${lineIdx}`}
              line={line}
              idx={lineIdx}
              highlighted={isLineHighlighted(lineIdx)}
              handleLineClick={handleLineClick}
            />
          </Element>
        );
      })}
    </pre>
  );
};

export default CodeText;
