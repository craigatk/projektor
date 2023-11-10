import * as React from "react";
import Accordion from "@material-ui/core/Accordion";
import AccordionDetails from "@material-ui/core/AccordionDetails";
import AccordionSummary from "@material-ui/core/AccordionSummary";
import AccordionActions from "@material-ui/core/AccordionActions";
import ExpandMoreIcon from "@material-ui/icons/ExpandMore";
import Button from "@material-ui/core/Button";
import Divider from "@material-ui/core/Divider";
import { makeStyles } from "@material-ui/core/styles";
import { AttachmentType, TestCase } from "../model/TestRunModel";
import { Typography } from "@material-ui/core";
import CleanLink from "../Link/CleanLink";
import { ExpandCollapseState } from "./ExpandCollapseState";
import TestCaseFailureScreenshot from "./TestCaseFailureScreenshot";
import {
  findAttachmentOfType,
  createTestCaseIdentifier,
} from "./testCaseHelpers";

const useStyles = makeStyles(() => ({
  panelActions: {
    justifyContent: "flex-start",
  },
  failureMessage: {
    backgroundColor: "#EDEDED",
    overflowX: "auto",
    fontSize: "0.9em",
  },
  panelSummary: {
    userSelect: "text",
    "-webkit-user-select": "text",
  },
}));

interface TestCaseFailurePanelProps {
  testCase: TestCase;
  publicId: string;
  expandCollapseAll?: ExpandCollapseState;
  showFullFailure?: boolean;
}

const TestCaseFailurePanel = ({
  testCase,
  publicId,
  expandCollapseAll,
  showFullFailure,
}: TestCaseFailurePanelProps) => {
  const classes = useStyles({});

  const testCaseIdentifier = createTestCaseIdentifier(testCase);

  const defaultExpanded =
    expandCollapseAll !== ExpandCollapseState.COLLAPSE_ALL;

  const [expanded, setExpanded] = React.useState<boolean>(defaultExpanded);

  React.useEffect(() => {
    setExpanded(defaultExpanded);
  }, [setExpanded, defaultExpanded]);

  const expansionPanelOnClick = () => {
    setExpanded(!expanded);
  };

  const screenshotAttachment = findAttachmentOfType(
    testCase,
    AttachmentType.IMAGE,
  );
  const videoAttachment = findAttachmentOfType(testCase, AttachmentType.VIDEO);

  let failureTextToShow = null;
  if (testCase.failure != null) {
    failureTextToShow = testCase.failure.failureText;

    if (!showFullFailure && testCase.failure.failureMessage) {
      failureTextToShow = testCase.failure.failureMessage;
    }
  }

  return (
    <Accordion
      expanded={expanded}
      data-testid={`test-case-summary-${testCaseIdentifier}`}
    >
      <AccordionSummary
        expandIcon={
          <span
            onClick={expansionPanelOnClick}
            data-testid={`test-case-expand-collapse-icon-${testCaseIdentifier}`}
          >
            <ExpandMoreIcon />
          </span>
        }
        className={classes.panelSummary}
        style={{ cursor: "auto" }}
        data-testid={`test-case-summary-header-${testCaseIdentifier}`}
      >
        <Typography
          variant="subtitle2"
          data-testid={`test-case-title-${testCaseIdentifier}`}
        >
          <span data-testid="test-case-title">
            {testCase.packageName || testCase.testSuiteName || ""}.
            {testCase.className} {testCase.name}
          </span>
        </Typography>
      </AccordionSummary>
      <AccordionDetails className={classes.failureMessage}>
        {testCase.failure != null && (
          <div>
            <pre data-testid={`test-case-failure-text-${testCaseIdentifier}`}>
              {failureTextToShow}
            </pre>

            <TestCaseFailureScreenshot
              testCase={testCase}
              publicId={publicId}
            />
          </div>
        )}
      </AccordionDetails>
      <Divider />
      <AccordionActions className={classes.panelActions}>
        {!testCase.passed && (
          <Button>
            <CleanLink
              to={`/tests/${publicId}/suite/${testCase.testSuiteIdx}/case/${testCase.idx}/failure`}
              data-testid={`test-case-summary-failure-link-${testCaseIdentifier}`}
            >
              Failure Details
            </CleanLink>
          </Button>
        )}
        {testCase.hasSystemOut && (
          <Button>
            <CleanLink
              to={`/tests/${publicId}/suite/${testCase.testSuiteIdx}/case/${testCase.idx}/systemOut`}
              data-testid={`test-case-summary-system-out-link-${testCaseIdentifier}`}
            >
              System Out
            </CleanLink>
          </Button>
        )}
        {testCase.hasSystemErr && (
          <Button>
            <CleanLink
              to={`/tests/${publicId}/suite/${testCase.testSuiteIdx}/case/${testCase.idx}/systemErr`}
              data-testid={`test-case-summary-system-err-link-${testCaseIdentifier}`}
            >
              System Err
            </CleanLink>
          </Button>
        )}
        {screenshotAttachment && (
          <Button>
            <CleanLink
              to={`/tests/${publicId}/suite/${testCase.testSuiteIdx}/case/${testCase.idx}/screenshot`}
              data-testid={`test-case-screenshot-link-${testCaseIdentifier}`}
            >
              Screenshot
            </CleanLink>
          </Button>
        )}
        {videoAttachment && (
          <Button>
            <CleanLink
              to={`/tests/${publicId}/suite/${testCase.testSuiteIdx}/case/${testCase.idx}/video`}
              data-testid={`test-case-video-link-${testCaseIdentifier}`}
            >
              Video
            </CleanLink>
          </Button>
        )}
      </AccordionActions>
    </Accordion>
  );
};

export default TestCaseFailurePanel;
