import * as React from "react";
import classes from "./CodeQualityReportTab.module.css";
import CodeText from "../CodeText/CodeText";
import { RouteComponentProps } from "@reach/router";
import { CodeQualityReport } from "../model/TestRunModel";

interface CodeQualityReportTabProps extends RouteComponentProps {
  codeQualityReportsWithContents: CodeQualityReport[];
  idx: string;
}

const CodeQualityReportTab = ({
  codeQualityReportsWithContents,
  idx,
}: CodeQualityReportTabProps) => {
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
