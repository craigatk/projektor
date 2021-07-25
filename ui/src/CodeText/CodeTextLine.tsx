import * as React from "react";
import styled from "styled-components";

interface CodeTextLineProps {
  line: string;
  idx: number;
  highlighted: boolean;
  handleLineClick(e: React.MouseEvent, lineIdx: number): void;
}

interface CodeTextLineStyleProps {
  highlighted: boolean;
}

const Line = styled.div<CodeTextLineStyleProps>`
  cursor: default;
  background-color: ${({ highlighted }) =>
    highlighted ? "#F9F9F9" : "inherit"};
  font-size: 0.9em;
  display: inline-block;
  width: 100%;
  padding-right: 10px;
`;

const LineNumber = styled.span`
  cursor: pointer;
  user-select: none;
  min-width: 40px;
  display: inline-block;
  text-align: right;
  padding-right: 15px;
  &:hover {
    text-decoration: underline;
    background-color: lightgrey;
  }
`;

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
    <Line
      data-testid={`code-text-line-${idx}-${highlighted}`}
      highlighted={highlighted}
    >
      <LineNumber
        data-testid={`code-text-line-number-${idx}`}
        onClick={handleClick}
      >
        {idx}
      </LineNumber>
      <span data-testid={`code-text-line-content-${idx}`}>{line}</span>
    </Line>
  );
};

export default CodeTextLine;
