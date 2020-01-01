import * as React from "react";
import { RouteComponentProps } from "@reach/router";
import { makeStyles } from "@material-ui/styles";
import { AppBar, Typography, Paper } from "@material-ui/core";

const useStyles = makeStyles(theme => ({
  appBar: {
    backgroundColor: "#1c313a",
    padding: "5px 10px"
  },
  appBarTitle: {
    flexGrow: 1
  },
  content: {
    flexGrow: 1,
    marginTop: "50px",
    textAlign: "center"
  },
  contentTitle: {
    paddingTop: "15px",
    paddingBottom: "15px"
  },
  paper: {
    maxWidth: "600px",
    margin: "auto",
    padding: "20px 40px"
  }
}));

const Welcome = (props: RouteComponentProps) => {
  const classes = useStyles({});

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
            <a href="https://github.com/craigatk/projektor/tree/master/publishers/gradle-plugin">
              Projektor Gradle plugin
            </a>{" "}
            or Javascript/Node script to easily handle the collection and
            publishing.
          </Typography>
        </Paper>
      </main>
    </div>
  );
};

export default Welcome;
