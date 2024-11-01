import * as React from "react";
import classes from "./RepositoryCoverageTimelineGraphTooltip.module.css";
import moment from "moment-timezone";

const RepositoryCoverageTimelineGraphTooltip = (props) => {
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
