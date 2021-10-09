import PageTitle from "../PageTitle";
import * as React from "react";
import CodeQualityReportTabs from "./CodeQualityReportTabs";
import { CodeQualityReports } from "../model/TestRunModel";

interface CodeQualityReportsSectionProps {
  publicId: string;
  codeQualityReports: CodeQualityReports;
}

const CodeQualityReportsSection = ({
  publicId,
  codeQualityReports,
}: CodeQualityReportsSectionProps) => {
  return (
    <div>
      <PageTitle title="Code quality" testid="code-quality-title" />

      <CodeQualityReportTabs
        codeQualityReports={codeQualityReports}
        publicId={publicId}
      />
    </div>
  );
};

export default CodeQualityReportsSection;
