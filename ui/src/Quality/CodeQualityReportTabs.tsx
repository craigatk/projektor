import * as React from "react";
import classes from "./CodeQualityReportTabs.module.css";
import { CodeQualityReport } from "../model/TestRunModel";
import {
  Link,
  Location,
  LocationContext,
  Redirect,
  Router,
} from "@reach/router";
import { getTabCurrentValue } from "../Tabs/TabValue";
import { Paper, Tab, Tabs } from "@material-ui/core";
import CodeQualityReportTab from "./CodeQualityReportTab";

interface CodeQualityReportTabsProps {
  publicId: string;
  codeQualityReportsWithContents: CodeQualityReport[];
}

const CodeQualityReportTabs = ({
  publicId,
  codeQualityReportsWithContents,
}: CodeQualityReportTabsProps) => {
  const linkBase = `/tests/${publicId}/quality/report`;

  const defaultTab = "/" + codeQualityReportsWithContents[0].idx;

  return (
    <div data-testid="code-quality-reports-tabs">
      <Paper elevation={1} className={classes.paper}>
        <Location>
          {({ location }: LocationContext) => (
            <Tabs
              value={getTabCurrentValue(location, defaultTab)}
              indicatorColor="primary"
              textColor="primary"
            >
              {codeQualityReportsWithContents.map((codeQualityReport) => (
                <Tab
                  className={classes.tab}
                  label={codeQualityReport.fileName}
                  value={"/" + codeQualityReport.idx}
                  data-testid={`code-quality-tab-${codeQualityReport.idx}`}
                  component={Link}
                  to={`${linkBase}/${codeQualityReport.idx}`}
                  key={`code-quality-tab-${codeQualityReport.idx}`}
                />
              ))}
            </Tabs>
          )}
        </Location>

        <Router>
          <Redirect from="/" to={`${linkBase}${defaultTab}`} noThrow={true} />
          <CodeQualityReportTab
            path="/report/:idx"
            codeQualityReportsWithContents={codeQualityReportsWithContents}
            idx="0"
          />
        </Router>
      </Paper>
    </div>
  );
};

export default CodeQualityReportTabs;
