import * as React from "react";
import { makeStyles } from "@material-ui/styles";

const useStyles = makeStyles((theme) => ({
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
    const { date, lineValue, branchValue } = props.payload[0].payload;

    return (
      <div className={classes.box}>
        <div className={classes.line}>
          <span className={classes.label}>Line coverage</span>
          {lineValue}%
        </div>
        <div className={classes.line}>
          <span className={classes.label}>Branch coverage</span>
          {branchValue}%
        </div>
        <div className={classes.line}>
          <span className={classes.label}>Run date</span>
          {date}
        </div>
      </div>
    );
  } else {
    return <span></span>;
  }
};

export default RepositoryCoverageTimelineGraphTooltip;
