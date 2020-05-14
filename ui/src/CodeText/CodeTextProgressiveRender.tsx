import * as React from "react";
import { makeStyles } from "@material-ui/core/styles";

interface CodeTextProgressiveRenderProps {
  listElements: any[];
  renderProgress: (number) => void;
  renderComplete: () => void;
  pageSize: number;
  lineHeight: number;
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
});

const CodeTextProgressiveRender = ({
  listElements,
  renderProgress,
  renderComplete,
  pageSize,
  lineHeight,
}: CodeTextProgressiveRenderProps) => {
  const [currentRenderLimit, setCurrentRenderLimit] = React.useState(pageSize);
  const maxRenderSize = listElements.length - 1;

  const classes = useStyles({
    lineCount: listElements.length,
    lineHeight,
  });

  const renderNextPage = () => {
    if (currentRenderLimit < maxRenderSize) {
      setCurrentRenderLimit(currentRenderLimit + pageSize);

      const renderProgressPercentage = Math.floor(
        (currentRenderLimit / maxRenderSize) * 100
      );
      renderProgress(renderProgressPercentage);
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
    <div className={classes.wrapper}>
      {listElements.slice(0, currentRenderLimit)}
    </div>
  );
};

export default CodeTextProgressiveRender;
