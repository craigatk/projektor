import * as React from "react";

interface CodeTextLinesChunkProps {
  lines: any[];
}

const CodeTextLinesChunk = ({ lines }: CodeTextLinesChunkProps) => {
  return <span>{lines}</span>;
};

export default CodeTextLinesChunk;
