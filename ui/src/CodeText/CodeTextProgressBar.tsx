import * as React from "react";
import classes from "./CodeTextProgressBar.module.css";
import { LinearProgress } from "@material-ui/core";

interface CodeTextProgressBarProps {
  currentValue: number;
  maxValue: number;
}

const CodeTextProgressBar = ({
  currentValue,
  maxValue,
}: CodeTextProgressBarProps) => {
  const renderProgressPercentage = Math.floor((currentValue / maxValue) * 100);

  return (
    <LinearProgress
      variant="determinate"
      value={renderProgressPercentage}
      className={classes.renderIndicator}
      data-testid="code-text-progress-render-loading-indicator"
    />
  );
};

export default CodeTextProgressBar;
