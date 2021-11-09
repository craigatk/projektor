import * as React from "react";
import { makeStyles } from "@material-ui/core/styles";
import { scroller } from "react-scroll";
import LinearProgress from "@material-ui/core/LinearProgress";

interface CodeTextProgressiveRenderProps {
  listElements: any[];
  pageSize: number;
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
  listElements,
  pageSize,
  lineHeight,
  highlightedLine,
}: CodeTextProgressiveRenderProps) => {
  const [currentRenderLimit, setCurrentRenderLimit] = React.useState(pageSize);
  const [rendered, setRendered] = React.useState(false);
  const [renderProgress, setRenderProgress] = React.useState(0);

  const lineCount = listElements.length;
  const maxRenderSize = lineCount - 1;

  const classes = useStyles({
    lineCount,
    lineHeight,
  });

  const renderComplete = () => {
    if (highlightedLine != null && !rendered) {
      scroller.scrollTo(`line-${highlightedLine}-true`, {
        duration: 0,
        delay: 0,
        offset: -45,
        smooth: "easeInOutQuart",
      });
    }

    setRendered(true);
  };

  const renderProgressUpdate = (progress: number) => {
    setRenderProgress(progress);
  };

  const renderNextPage = () => {
    if (currentRenderLimit < maxRenderSize) {
      setCurrentRenderLimit(currentRenderLimit + pageSize);

      const renderProgressPercentage = Math.floor(
        (currentRenderLimit / maxRenderSize) * 100
      );
      renderProgressUpdate(renderProgressPercentage);
    } else {
      renderComplete();
    }
  };

  if ("requestIdleCallback" in window) {
    // @ts-ignore
    window.requestIdleCallback(renderNextPage);
  } else {
    setTimeout(renderNextPage);
  }

  return (
    <div>
      {!rendered && listElements.length > 1000 ? (
        <LinearProgress
          variant="determinate"
          value={renderProgress}
          className={classes.renderIndicator}
        />
      ) : null}
      <div className={classes.wrapper}>
        {listElements.slice(0, currentRenderLimit)}
      </div>
    </div>
  );
};

export default CodeTextProgressiveRender;
