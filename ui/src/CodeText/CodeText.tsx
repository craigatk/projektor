import * as React from "react";
import { useQueryParam, NumberParam } from "use-query-params";
import { Element } from "react-scroll";
import CodeTextProgressiveRender from "./CodeTextProgressiveRender";
import CodeTextLine from "./CodeTextLine";
import styled from "styled-components";
import _ from "lodash";
import CodeTextLinesChunk from "./CodeTextLinesChunk";

interface CodeTextProps {
  text: string;
}

const LineElement = styled(Element)`
  padding-right: 10px;
`;

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
      <LineElement
        name={`line-${lineIdx}-${highlighted}`}
        key={`line-element-${lineIdx}-${highlighted}`}
      >
        <CodeTextLine
          key={`code-line-${lineIdx}-${highlighted}`}
          line={line}
          idx={lineIdx}
          highlighted={highlighted}
          handleLineClick={handleLineClick}
        />
      </LineElement>
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
