import * as React from "react";
import CodeText from "../CodeText/CodeText";
import { RouteComponentProps } from "@reach/router";
import { CodeQualityReports } from "../model/TestRunModel";
import { makeStyles } from "@material-ui/core/styles";

interface CodeQualityReportTabProps extends RouteComponentProps {
  codeQualityReports: CodeQualityReports;
  reportFileName: string;
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
  codeQualityReports,
  reportFileName,
}: CodeQualityReportTabProps) => {
  const classes = useStyles({});

  const codeQualityReport = codeQualityReports.reports.find(
    (report) => report.fileName === reportFileName
  );
  const reportContents = codeQualityReport ? codeQualityReport.contents : "";

  return (
    <div className={classes.reportContents}>
      <CodeText text={reportContents} />
    </div>
  );
};

export default CodeQualityReportTab;
