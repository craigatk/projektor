import PageTitle from "../PageTitle";
import * as React from "react";
import CodeQualityReportTabs from "./CodeQualityReportTabs";
import { CodeQualityReports } from "../model/TestRunModel";
import { makeStyles } from "@material-ui/core/styles";
import { Typography } from "@material-ui/core";

interface CodeQualityReportsSectionProps {
  publicId: string;
  codeQualityReports: CodeQualityReports;
}

const useStyles = makeStyles(() => ({
  noReports: {
    marginTop: "20px",
    marginLeft: "15px",
  },
}));

const CodeQualityReportsSection = ({
  publicId,
  codeQualityReports,
}: CodeQualityReportsSectionProps) => {
  const classes = useStyles({});

  const reportsWithContents =
    codeQualityReports && codeQualityReports.reports
      ? codeQualityReports.reports.filter(
          (report) => report.contents && report.contents.length > 0
        )
      : [];

  const hasReportsWithContents = reportsWithContents.length > 0;

  return (
    <div>
      <PageTitle title="Code quality" testid="code-quality-title" />

      {hasReportsWithContents && (
        <CodeQualityReportTabs
          codeQualityReportsWithContents={reportsWithContents}
          publicId={publicId}
        />
      )}

      {!hasReportsWithContents && (
        <Typography
          data-testid="code-quality-reports-not-found"
          className={classes.noReports}
        >
          No code quality reports found
        </Typography>
      )}
    </div>
  );
};

export default CodeQualityReportsSection;
