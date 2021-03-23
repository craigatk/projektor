import * as React from "react";
import { TestCase } from "../model/TestRunModel";
import TestCaseFailurePanel from "./TestCaseFailurePanel";
import { ExpandCollapseState } from "./ExpandCollapseState";
import UnfoldLessOutlinedIcon from "@material-ui/icons/UnfoldLessOutlined";
import UnfoldMoreOutlinedIcon from "@material-ui/icons/UnfoldMoreOutlined";
import { Typography } from "@material-ui/core";
import CleanLinkText from "../Link/CleanLinkText";
import { makeStyles } from "@material-ui/core/styles";

const showFullFailureMaxFailureCount = 5;

interface TestCaseFailurePanelListProps {
  failedTestCases: TestCase[];
  publicId: string;
}

const useStyles = makeStyles(() => ({
  expandCollapseLink: {
    display: "inline-block",
    marginBottom: "10px",
    marginLeft: "7px",
  },
  expandCollapseIcon: {
    display: "inline-block",
    verticalAlign: "middle",
  },
  expandCollapseLabel: {
    display: "inline-block",
  },
}));

const TestCaseFailurePanelList = ({
  failedTestCases,
  publicId,
}: TestCaseFailurePanelListProps) => {
  const classes = useStyles({});

  const initialExpandCollapseState =
    failedTestCases.length <= showFullFailureMaxFailureCount
      ? ExpandCollapseState.EXPAND_ALL
      : ExpandCollapseState.COLLAPSE_ALL;
  const [expandCollapseAll, setExpandCollapseState] = React.useState<
    ExpandCollapseState
  >(initialExpandCollapseState);

  const expandCollapseOnClick = () => {
    if (expandCollapseAll === ExpandCollapseState.COLLAPSE_ALL) {
      setExpandCollapseState(ExpandCollapseState.EXPAND_ALL);
    } else {
      setExpandCollapseState(ExpandCollapseState.COLLAPSE_ALL);
    }
  };

  return (
    <div>
      <Typography variant="body2">
        <CleanLinkText onClick={expandCollapseOnClick}>
          {expandCollapseAll === ExpandCollapseState.COLLAPSE_ALL && (
            <span
              className={classes.expandCollapseLink}
              data-testid="test-failure-expand-all-link"
            >
              <UnfoldMoreOutlinedIcon className={classes.expandCollapseIcon} />
              <span className={classes.expandCollapseLabel}>
                Expand all failure details
              </span>
            </span>
          )}
          {expandCollapseAll === ExpandCollapseState.EXPAND_ALL && (
            <span
              className={classes.expandCollapseLink}
              data-testid="test-failure-collapse-all-link"
            >
              <UnfoldLessOutlinedIcon className={classes.expandCollapseIcon} />
              <span className={classes.expandCollapseLabel}>
                Collapse all failure details
              </span>
            </span>
          )}
        </CleanLinkText>
      </Typography>
      <div>
        {failedTestCases.map((testCase) => (
          <TestCaseFailurePanel
            testCase={testCase}
            publicId={publicId}
            key={`test-case-${testCase.testSuiteIdx}-${testCase.idx}`}
            showFullFailure={
              failedTestCases.length <= showFullFailureMaxFailureCount
            }
            expandCollapseAll={expandCollapseAll}
          />
        ))}
      </div>
    </div>
  );
};

export default TestCaseFailurePanelList;
