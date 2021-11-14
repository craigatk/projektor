import * as React from "react";
import { makeStyles } from "@material-ui/core/styles";
import { scroller } from "react-scroll";
import CodeTextProgressBar from "./CodeTextProgressBar";

interface CodeTextProgressiveRenderProps {
  lineChunks: any[];
  lineCount: number;
  lineHeight: number;
  highlightedLine: number;
}

interface CodeTextProgressiveRenderStyleProps {
  lineCount: number;
  lineHeight: number;
}

const useStyles = makeStyles({
  wrapper: ({
    lineCount,
    lineHeight,
  }: CodeTextProgressiveRenderStyleProps) => ({
    height: lineCount * lineHeight,
    display: "inline-block",
    width: "fit-content",
    minWidth: "100%",
  }),
  renderIndicator: {
    marginLeft: "40px",
    marginRight: "40px",
    marginBottom: "20px",
  },
});

const CodeTextProgressiveRender = ({
  lineChunks,
  lineCount,
  lineHeight,
  highlightedLine,
}: CodeTextProgressiveRenderProps) => {
  const [chunkIdx, setChunkIdx] = React.useState(1);
  const [rendered, setRendered] = React.useState(false);

  const classes = useStyles({
    lineCount,
    lineHeight,
  });

  const renderComplete = () => {
    if (!rendered) {
      if (highlightedLine != null) {
        scroller.scrollTo(`line-${highlightedLine}-true`, {
          duration: 0,
          delay: 0,
          offset: -45,
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
      <div className={classes.wrapper}>{lineChunks.slice(0, chunkIdx)}</div>
    </div>
  );
};

export default CodeTextProgressiveRender;
