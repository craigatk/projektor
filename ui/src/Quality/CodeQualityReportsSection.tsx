import PageTitle from "../PageTitle";
import * as React from "react";
import CodeQualityReportTabs from "./CodeQualityReportTabs";
import { CodeQualityReports } from "../model/TestRunModel";
import { makeStyles } from "@material-ui/core/styles";
import { Typography } from "@material-ui/core";
import CleanLinkText from "../Link/CleanLinkText";

interface CodeQualityReportsSectionProps {
  publicId: string;
  codeQualityReports: CodeQualityReports;
}

const useStyles = makeStyles(() => ({
  noReports: {
    marginTop: "20px",
    marginLeft: "15px",
    textAlign: "center",
  },
  noReportsDocSection: {
    marginTop: "20px",
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

  const reportsWithoutContents =
    codeQualityReports && codeQualityReports.reports
      ? codeQualityReports.reports.filter(
          (report) => !report.contents || report.contents.length === 0
        )
      : [];
  const hasReportsWithoutContents = reportsWithoutContents.length > 0;

  return (
    <div>
      <PageTitle title="Code quality" testid="code-quality-title" />

      {hasReportsWithContents && (
        <CodeQualityReportTabs
          codeQualityReportsWithContents={reportsWithContents}
          publicId={publicId}
        />
      )}

      {!hasReportsWithContents && hasReportsWithoutContents && (
        <Typography
          data-testid="code-quality-reports-all-passed"
          className={classes.noReports}
        >
          <div>All code quality reports passed!</div>
        </Typography>
      )}

      {!hasReportsWithContents && !hasReportsWithoutContents && (
        <Typography
          data-testid="code-quality-reports-not-found"
          className={classes.noReports}
        >
          <div>No code quality reports included as part of this report.</div>
          <div className={classes.noReportsDocSection}>
            To include code quality reports in your report,{" "}
            <CleanLinkText href="https://projektor.dev/docs/code-quality/">
              please see the Projektor code quality docs
            </CleanLinkText>
            .
          </div>
        </Typography>
      )}
    </div>
  );
};

export default CodeQualityReportsSection;
