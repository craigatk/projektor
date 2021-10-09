import * as React from "react";
import { CodeQualityReports } from "../model/TestRunModel";
import { makeStyles } from "@material-ui/core/styles";
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
  codeQualityReports: CodeQualityReports;
}

const useStyles = makeStyles((theme) => ({
  paper: {
    padding: theme.spacing(1, 2),
  },
  detailsSection: {
    paddingTop: "20px",
  },
  tab: {
    textTransform: "none",
    maxWidth: "300px",
  },
}));

const CodeQualityReportTabs = ({
  publicId,
  codeQualityReports,
}: CodeQualityReportTabsProps) => {
  const classes = useStyles({});
  const linkBase = `/tests/${publicId}/quality/report`;

  if (codeQualityReports && codeQualityReports.reports) {
    const defaultTab = "/" + codeQualityReports.reports[0].fileName;

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
                {codeQualityReports.reports.map((codeQualityReport) => (
                  <Tab
                    className={classes.tab}
                    label={codeQualityReport.fileName}
                    value={"/" + codeQualityReport.fileName}
                    data-testid={`code-quality-tab-${codeQualityReport.fileName}`}
                    component={Link}
                    to={`${linkBase}/${codeQualityReport.fileName}`}
                    key={`tab-${codeQualityReport.fileName}`}
                  />
                ))}
              </Tabs>
            )}
          </Location>

          <Router>
            <Redirect from="/" to={`${linkBase}${defaultTab}`} noThrow={true} />
            <CodeQualityReportTab
              path="/report/:reportFileName"
              codeQualityReports={codeQualityReports}
              reportFileName=""
            />
          </Router>
        </Paper>
      </div>
    );
  } else {
    return <div></div>;
  }
};

export default CodeQualityReportTabs;
