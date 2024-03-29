import * as React from "react";
import { makeStyles } from "@material-ui/styles";
import moment from "moment-timezone";

const useStyles = makeStyles(() => ({
  box: {
    outline: "1px solid black",
    backgroundColor: "white",
    padding: "5px 10px",
  },
  label: {
    width: "120px",
    display: "inline-block",
  },
  line: {
    paddingBottom: "4px",
    paddingTop: "4px",
  },
}));

const RepositoryCoverageTimelineGraphTooltip = (props) => {
  const classes = useStyles({});

  if (props.payload && props.payload.length >= 1) {
    const { createdTimestamp, lineValue, branchValue } =
      props.payload[0].payload;

    return (
      <div
        className={classes.box}
        data-testid="coverage-timeline-graph-tooltip"
      >
        <div className={classes.line}>
          <span className={classes.label}>Line coverage</span>
          <span data-testid="tooltip-line-coverage-percentage">
            {lineValue}%
          </span>
        </div>
        <div className={classes.line}>
          <span className={classes.label}>Branch coverage</span>
          <span data-testid="tooltip-branch-coverage-percentage">
            {branchValue}%
          </span>
        </div>
        <div className={classes.line}>
          <span className={classes.label}>Run date</span>
          <span data-testid="tooltip-run-date">
            {moment(createdTimestamp).format("MMM Do YYYY h:mm a")}
          </span>
        </div>
      </div>
    );
  } else {
    return <span data-testid="empty-tooltip" />;
  }
};

export default RepositoryCoverageTimelineGraphTooltip;
