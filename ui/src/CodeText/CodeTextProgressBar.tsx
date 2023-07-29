import * as React from "react";
import { makeStyles } from "@mui/material/styles";
import { LinearProgress } from "@mui/material";

interface CodeTextProgressBarProps {
  currentValue: number;
  maxValue: number;
}

const useStyles = makeStyles({
  renderIndicator: {
    marginLeft: "40px",
    marginRight: "40px",
    marginBottom: "20px",
  },
});

const CodeTextProgressBar = ({
  currentValue,
  maxValue,
}: CodeTextProgressBarProps) => {
  const classes = useStyles({});

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
