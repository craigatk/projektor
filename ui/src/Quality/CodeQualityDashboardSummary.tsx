import * as React from "react";
import PageTitle from "../PageTitle";
import { fetchCodeQualityReports } from "../service/TestRunService";
import { CodeQualityReports } from "../model/TestRunModel";
import CleanLink from "../Link/CleanLink";
import { List, ListItem, Typography } from "@material-ui/core";

interface CodeQualityDashboardSummaryProps {
  publicId: string;
}

const CodeQualityDashboardSummary = ({
  publicId,
}: CodeQualityDashboardSummaryProps) => {
  const [codeQualityReports, setCodeQualityReports] =
    React.useState<CodeQualityReports>(null);

  React.useEffect(() => {
    fetchCodeQualityReports(publicId)
      .then((response) => {
        setCodeQualityReports(response.data);
      })
      .catch(() => {});
  }, [setCodeQualityReports]);

  const reportsWithContents =
    codeQualityReports && codeQualityReports.reports
      ? codeQualityReports.reports.filter(
          (report) => report.contents && report.contents.length > 0,
        )
      : [];
  const hasReportsWithContents = reportsWithContents.length > 0;

  if (hasReportsWithContents) {
    return (
      <div>
        <CleanLink to={`/tests/${publicId}/quality`}>
          <PageTitle title="Code quality" testid="code-quality-summary-title" />
        </CleanLink>
        <List>
          {reportsWithContents.map((report, idx) => (
            <ListItem key={`code-quality-summary-item-${idx}`}>
              <Typography variant="body2">
                File:{" "}
                <CleanLink
                  to={`/tests/${publicId}/quality/report/${report.idx}`}
                >
                  {report.fileName}
                </CleanLink>
              </Typography>
            </ListItem>
          ))}
        </List>
      </div>
    );
  } else {
    return null;
  }
};

export default CodeQualityDashboardSummary;
