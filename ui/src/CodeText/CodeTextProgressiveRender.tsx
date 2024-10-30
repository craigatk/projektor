import * as React from "react";
import classes from "./CodeTextProgressiveRender.module.css";
import { scroller } from "react-scroll";
import CodeTextProgressBar from "./CodeTextProgressBar";

interface CodeTextProgressiveRenderProps {
  lineChunks: any[];
  lineCount: number;
  lineHeight: number;
  highlightedLine: number;
}

const CodeTextProgressiveRender = ({
  lineChunks,
  lineCount,
  lineHeight,
  highlightedLine,
}: CodeTextProgressiveRenderProps) => {
  const [chunkIdx, setChunkIdx] = React.useState(1);
  const [rendered, setRendered] = React.useState(false);

  const renderComplete = () => {
    if (!rendered) {
      if (highlightedLine != null) {
        scroller.scrollTo(`line-${highlightedLine}-true`, {
          duration: 0,
          delay: 0,
          offset: -70,
          smooth: "easeInOutQuart",
        });
      }

      setRendered(true);
    }
  };

  const renderNextPage = () => {
    if (chunkIdx < lineChunks.length) {
      setChunkIdx(chunkIdx + 1);
    } else {
      renderComplete();
    }
  };

  if (!rendered) {
    setTimeout(renderNextPage);
  }

  return (
    <div>
      {!rendered && lineChunks.length > 2 && (
        <CodeTextProgressBar
          currentValue={chunkIdx}
          maxValue={lineChunks.length}
        />
      )}
      <div
        className={classes.wrapper}
        style={{ height: lineCount * lineHeight }}
      >
        {lineChunks.slice(0, chunkIdx)}
      </div>
    </div>
  );
};

export default CodeTextProgressiveRender;
