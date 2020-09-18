import * as React from "react";
import { Dot } from "recharts";
import { navigate } from "@reach/router";
import { makeStyles } from "@material-ui/styles";

const useStyles = makeStyles(() => ({
  dot: {
    "&:hover": {
      cursor: "pointer",
    },
  },
}));

const RepositoryCoverageTimelineGraphActiveDot = (props) => {
  const classes = useStyles({});
  const dotOnClick = () => {
    const publicId = props.payload.publicId;
    navigate(`/tests/${publicId}`);
  };
  return (
    <Dot
      r={8}
      cy={props.cy}
      cx={props.cx}
      fill={props.fill}
      onClick={dotOnClick}
      className={classes.dot}
    />
  );
};

export default RepositoryCoverageTimelineGraphActiveDot;
