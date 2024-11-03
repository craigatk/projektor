import * as React from "react";
import classes from "./TestCaseFailurePanelList.module.css";
import { TestCase } from "../model/TestRunModel";
import TestCaseFailurePanel from "./TestCaseFailurePanel";
import { ExpandCollapseState } from "./ExpandCollapseState";
import UnfoldLessOutlinedIcon from "@mui/icons-material/UnfoldLessOutlined";
import UnfoldMoreOutlinedIcon from "@mui/icons-material/UnfoldMoreOutlined";
import { Typography } from "@mui/material";
import CleanLinkText from "../Link/CleanLinkText";

const showFullFailureMaxFailureCount = 5;
const expandAllFailuresCount = 15;

interface TestCaseFailurePanelListProps {
  failedTestCases: TestCase[];
  publicId: string;
}

const TestCaseFailurePanelList = ({
  failedTestCases,
  publicId,
}: TestCaseFailurePanelListProps) => {
  const initialExpandCollapseState =
    failedTestCases.length <= expandAllFailuresCount
      ? ExpandCollapseState.EXPAND_ALL
      : ExpandCollapseState.COLLAPSE_ALL;
  const [expandCollapseAll, setExpandCollapseState] =
    React.useState<ExpandCollapseState>(initialExpandCollapseState);

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
