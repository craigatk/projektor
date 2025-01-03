import * as React from "react";
import { Dot } from "recharts";
import { navigate } from "@reach/router";
import classes from "./RepositoryGraphActiveDot.module.css";

const RepositoryGraphActiveDot = (props) => {
  const { cy, cx, fill, dataKey, payload } = props;
  const { publicId } = payload;

  const dotOnClick = () => {
    navigate(`/tests/${publicId}`);
  };
  return (
    <Dot
      r={8}
      cy={cy}
      cx={cx}
      fill={fill}
      onClick={dotOnClick}
      className={classes.dot}
      role={`active-dot-${dataKey}-${publicId}`}
    />
  );
};

export default RepositoryGraphActiveDot;
