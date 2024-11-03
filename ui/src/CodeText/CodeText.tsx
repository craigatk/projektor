import * as React from "react";
import { useQueryParam, NumberParam } from "use-query-params";
import classes from "./CodeText.module.css";
import { Element } from "react-scroll";
import CodeTextProgressiveRender from "./CodeTextProgressiveRender";
import CodeTextLine from "./CodeTextLine";
import _ from "lodash";
import CodeTextLinesChunk from "./CodeTextLinesChunk";

interface CodeTextProps {
  text: string;
}
const CodeText = ({ text }: CodeTextProps) => {
  if (text == null) {
    return null;
  }

  const [highlightedLine, setHighlightedLine] = useQueryParam("l", NumberParam);

  const textLines = text.split(/\r?\n/g);
  const lastLine = textLines.pop();
  if (lastLine !== "") {
    textLines.push(lastLine);
  }

  function handleLineClick(event: React.MouseEvent, clickedIdx: number) {
    if (highlightedLine == null) {
      setHighlightedLine(clickedIdx);
    } else if (clickedIdx !== highlightedLine) {
      setHighlightedLine(clickedIdx);
    } else {
      setHighlightedLine(null);
    }
  }

  React.useEffect(() => {
    return () => {
      setHighlightedLine(null);
    };
  }, [setHighlightedLine]);

  const lineElements = textLines.map((line, idx) => {
    const lineIdx = idx + 1;
    const highlighted = lineIdx === highlightedLine;

    return (
      <Element
        name={`line-${lineIdx}-${highlighted}`}
        key={`line-element-${lineIdx}-${highlighted}`}
        className={classes.line}
      >
        <CodeTextLine
          key={`code-line-${lineIdx}-${highlighted}`}
          line={line}
          idx={lineIdx}
          highlighted={highlighted}
          handleLineClick={handleLineClick}
        />
      </Element>
    );
  });

  const lineCount = lineElements.length;
  const pageSize = 1000;
  const lineChunks = _.chunk(lineElements, pageSize).map((lines, idx) => (
    <CodeTextLinesChunk lines={lines} key={`chunk-${idx}`} />
  ));

  const lineHeight = 16;

  return (
    <pre data-testid="code-text">
      <CodeTextProgressiveRender
        lineCount={lineCount}
        lineChunks={lineChunks}
        lineHeight={lineHeight}
        highlightedLine={highlightedLine}
      />
    </pre>
  );
};

export default CodeText;
