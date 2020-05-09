import * as React from "react";
import { useQueryParam, NumberParam } from "use-query-params";
import { scroller } from "react-scroll";
import CodeTextProgressiveRender from "./CodeTextProgressiveRender";

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
  const [alreadyScrolled, setAlreadyScrolled] = React.useState(false);

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
    return () => {
      setHighlightedLine(null);
      setHighlightedRangeEnd(null);
    };
  }, [setHighlightedLine, setHighlightedRangeEnd]);

  const renderComplete = () => {
    if (highlightedLine != null && !alreadyScrolled) {
      scroller.scrollTo(`line-${highlightedLine}`, {
        duration: 0,
        delay: 0,
        offset: -55,
        smooth: "easeInOutQuart",
      });

      setAlreadyScrolled(true);
    }
  };

  function isLineHighlighted(lineIdx: number) {
    if (highlightedRangeEnd != null) {
      return lineIdx >= highlightedLine && lineIdx <= highlightedRangeEnd;
    } else {
      return lineIdx === highlightedLine;
    }
  }

  return (
    <pre data-testid="code-text">
      <CodeTextProgressiveRender
        lines={textLines}
        isLineHighlighted={isLineHighlighted}
        handleLineClick={handleLineClick}
        highlightedLine={highlightedLine}
        highlightedRangeEnd={highlightedRangeEnd}
        renderComplete={renderComplete}
        pageSize={300}
      />
    </pre>
  );
};

export default CodeText;
