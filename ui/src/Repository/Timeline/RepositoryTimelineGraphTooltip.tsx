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

const RepositoryTimelineGraphTooltip = (props) => {
  const classes = useStyles({});

  if (props.payload && props.payload.length >= 1) {
    const {
      createdTimestamp,
      duration,
      totalTestCount,
    } = props.payload[0].payload;

    return (
      <div className={classes.box} data-testid="timeline-graph-tooltip">
        <div className={classes.line}>
          <span className={classes.label}>Test execution time</span>
          <span data-testid="timeline-tooltip-duration">{duration}s</span>
        </div>
        <div className={classes.line}>
          <span className={classes.label}>Test count</span>
          <span data-testid="timeline-tooltip-test-count">
            {totalTestCount} tests
          </span>
        </div>
        <div className={classes.line}>
          <span className={classes.label}>Run date</span>
          <span data-testid="timeline-tooltip-run-date">
            {moment(createdTimestamp).format("MMM Do YYYY, h:mm a")}
          </span>
        </div>
      </div>
    );
  } else {
    return <span data-testid="empty-timeline-tooltip" />;
  }
};

export default RepositoryTimelineGraphTooltip;
