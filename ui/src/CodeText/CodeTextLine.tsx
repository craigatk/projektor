import * as React from "react";
import styled from "styled-components";
import LinkOutlinedIcon from "@material-ui/icons/LinkOutlined";

interface CodeTextLineProps {
  line: string;
  idx: number;
  highlighted: boolean;
  handleLineClick(e: React.MouseEvent, lineIdx: number): void;
}

interface CodeTextLineStyleProps {
  highlighted: boolean;
}

const LinkIcon = styled(LinkOutlinedIcon)`
  visibility: hidden;
  color: blue;
  height: 0.8em;
  vertical-align: middle;
  padding-left: 5px;
`;

const Line = styled.div<CodeTextLineStyleProps>`
  background-color: ${({ highlighted }) =>
    highlighted ? "#F9F9F9" : "inherit"};
  font-size: 0.9em;
  display: inline-block;
  width: 100%;
  padding-right: 10px;
  &:hover {
    background-color: lightgrey;

    & ${LinkIcon} {
      visibility: visible;
    }
  }
`;

const LineNumber = styled.span`
  cursor: pointer;
  user-select: none;
  display: inline-block;
  padding-right: 15px;
  &:hover {
    color: blue;
  }
`;

const LineNumberValue = styled.span`
  text-align: right;
  display: inline-block;
  min-width: 30px;
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
        <LinkIcon fontSize="small" />
        <LineNumberValue>{idx}</LineNumberValue>
      </LineNumber>
      <span data-testid={`code-text-line-content-${idx}`}>{line}</span>
    </Line>
  );
};

export default CodeTextLine;
