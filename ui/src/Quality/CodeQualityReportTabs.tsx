import * as React from "react";
import { CodeQualityReport } from "../model/TestRunModel";
import { makeStyles } from "@mui/material/styles";
import {
  Link,
  Location,
  LocationContext,
  Redirect,
  Router,
} from "@reach/router";
import { getTabCurrentValue } from "../Tabs/TabValue";
import { Paper, Tab, Tabs } from "@mui/material";
import CodeQualityReportTab from "./CodeQualityReportTab";

interface CodeQualityReportTabsProps {
  publicId: string;
  codeQualityReportsWithContents: CodeQualityReport[];
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
  codeQualityReportsWithContents,
}: CodeQualityReportTabsProps) => {
  const classes = useStyles({});
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
