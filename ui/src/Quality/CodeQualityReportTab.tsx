import * as React from "react";
import CodeText from "../CodeText/CodeText";
import { RouteComponentProps } from "@reach/router";
import { CodeQualityReport } from "../model/TestRunModel";
import { makeStyles } from "@material-ui/core/styles";

interface CodeQualityReportTabProps extends RouteComponentProps {
  codeQualityReportsWithContents: CodeQualityReport[];
  idx: string;
}

const useStyles = makeStyles(() => ({
  reportContents: {
    marginTop: "20px",
    padding: "10px 0",
    backgroundColor: "#EDEDED",
    borderRadius: "8px",
    overflowX: "auto",
    fontSize: "0.9em",
  },
}));

const CodeQualityReportTab = ({
  codeQualityReportsWithContents,
  idx,
}: CodeQualityReportTabProps) => {
  const classes = useStyles({});

  const codeQualityReport = codeQualityReportsWithContents.find(
    (report) => report.idx === parseInt(idx),
  );
  const reportContents = codeQualityReport ? codeQualityReport.contents : "";

  return (
    <div className={classes.reportContents}>
      <CodeText text={reportContents} />
    </div>
  );
};

export default CodeQualityReportTab;
