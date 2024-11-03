import * as React from "react";
import { RouteComponentProps } from "@reach/router";
import { AppBar, Typography, Paper } from "@mui/material";
import classes from "./Welcome.module.css";

const Welcome = (props: RouteComponentProps) => {
  return (
    <div>
      <AppBar className={classes.appBar}>
        <Typography variant="h6" className={classes.appBarTitle}>
          Projektor
        </Typography>
      </AppBar>
      <main className={classes.content}>
        <Paper className={classes.paper}>
          <Typography
            variant="h3"
            className={classes.contentTitle}
            data-testid="welcome-title"
          >
            Welcome to Projektor
          </Typography>

          <Typography>
            To get started, publish your JUnit-format XML test results to the
            "/results" endpoint or use the{" "}
            <a href="https://projektor.dev/docs/gradle-plugin/">
              Projektor Gradle plugin
            </a>{" "}
            or{" "}
            <a href="https://projektor.dev/docs/node-script/">
              Javascript/Node script
            </a>{" "}
            to easily handle the collection and publishing.
          </Typography>
        </Paper>
      </main>
    </div>
  );
};

export default Welcome;
