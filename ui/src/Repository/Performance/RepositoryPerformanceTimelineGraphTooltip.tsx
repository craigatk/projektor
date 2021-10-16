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

const RepositoryPerformanceTimelineGraphTooltip = (props) => {
  const classes = useStyles({});

  if (props.payload && props.payload.length >= 1) {
    const { createdTimestamp, average, p95, maximum, requestsPerSecond } =
      props.payload[0].payload;

    return (
      <div
        className={classes.box}
        data-testid="performance-timeline-graph-tooltip"
      >
        <div className={classes.line}>
          <span className={classes.label}>Average</span>
          <span data-testid="performance-timeline-tooltip-average">
            {average}ms
          </span>
        </div>
        <div className={classes.line}>
          <span className={classes.label}>p95</span>
          <span data-testid="performance-timeline-tooltip-p95">{p95}ms</span>
        </div>
        <div className={classes.line}>
          <span className={classes.label}>Max</span>
          <span data-testid="performance-timeline-tooltip-max">
            {maximum}ms
          </span>
        </div>
        <div className={classes.line}>
          <span className={classes.label}>Requests per second</span>
          <span data-testid="performance-timeline-tooltip-rps">
            {requestsPerSecond}
          </span>
        </div>
        <div className={classes.line}>
          <span className={classes.label}>Run date</span>
          <span data-testid="performance-timeline-tooltip-run-date">
            {moment(createdTimestamp).format("MMM Do YYYY, h:mm a")}
          </span>
        </div>
      </div>
    );
  } else {
    return <span data-testid="empty-timeline-tooltip" />;
  }
};

export default RepositoryPerformanceTimelineGraphTooltip;
