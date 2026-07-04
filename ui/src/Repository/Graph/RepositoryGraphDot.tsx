import * as React from "react";
import { Dot } from "recharts";
import { navigate } from "@reach/router";
import classes from "./RepositoryGraphActiveDot.module.css";

const RepositoryGraphDot = (props) => {
  const { dataKey, payload, className } = props;
  const { publicId } = payload;
  const value = payload[dataKey];

  const dotOnClick = () => {
    navigate(`/tests/${publicId}`);
  };

  return (
    <Dot
      {...props}
      onClick={dotOnClick}
      className={className ? `${className} ${classes.dot}` : classes.dot}
      role={`dot-${dataKey}-${publicId}`}
      name={`dot-${dataKey}-${value}`}
    />
  );
};

export default RepositoryGraphDot;
