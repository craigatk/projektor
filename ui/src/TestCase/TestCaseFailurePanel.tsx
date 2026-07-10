import * as React from "react";
import classes from "./TestCaseFailurePanel.module.css";
import Accordion from "@mui/material/Accordion";
import AccordionDetails from "@mui/material/AccordionDetails";
import AccordionSummary from "@mui/material/AccordionSummary";
import AccordionActions from "@mui/material/AccordionActions";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import Button from "@mui/material/Button";
import Divider from "@mui/material/Divider";
import { AttachmentType, TestCase } from "../model/TestRunModel";
import { Chip, Fade, IconButton, Tooltip, Typography } from "@mui/material";
import FileCopyOutlinedIcon from "@mui/icons-material/FileCopyOutlined";
import CleanLink from "../Link/CleanLink";
import { ExpandCollapseState } from "./ExpandCollapseState";
import TestCaseFailureScreenshot from "./TestCaseFailureScreenshot";
import {
  findAttachmentOfType,
  createTestCaseIdentifier,
} from "./testCaseHelpers";
import { AIContext, AIState } from "../AI/AIContext";
import AIIcon from "../Icons/AIIcon";
import { fetchTestCaseDebugContext } from "../service/TestRunService";

enum CopyAIContextState {
  IDLE,
  COPYING,
  COPIED,
  ERROR,
}

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
  const { aiConfig } = React.useContext(AIContext);

  const testCaseIdentifier = createTestCaseIdentifier(testCase);

  const defaultExpanded =
    expandCollapseAll !== ExpandCollapseState.COLLAPSE_ALL;

  const [expanded, setExpanded] = React.useState<boolean>(defaultExpanded);
  const [copyAIContextState, setCopyAIContextState] =
    React.useState<CopyAIContextState>(CopyAIContextState.IDLE);

  React.useEffect(() => {
    setExpanded(defaultExpanded);
  }, [setExpanded, defaultExpanded]);

  const expansionPanelOnClick = () => {
    setExpanded(!expanded);
  };

  const copyAIContext = () => {
    setCopyAIContextState(CopyAIContextState.COPYING);

    fetchTestCaseDebugContext(publicId, testCase.testSuiteIdx, testCase.idx)
      .then((response) => navigator.clipboard.writeText(response.data.markdown))
      .then(() => setCopyAIContextState(CopyAIContextState.COPIED))
      .catch(() => setCopyAIContextState(CopyAIContextState.ERROR))
      .finally(() => {
        setTimeout(() => setCopyAIContextState(CopyAIContextState.IDLE), 3000);
      });
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
          {testCase.fileName && (
            <span
              className={classes.testCaseFileName}
              data-testid={`test-case-file-name-${testCaseIdentifier}`}
            >
              {testCase.fileName}
            </span>
          )}
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
        {aiConfig && aiConfig.testCaseFailureAnalysisEnabled && (
          <Button>
            <CleanLink
              to={`/tests/${publicId}/suite/${testCase.testSuiteIdx}/case/${testCase.idx}/analysis`}
              data-testid={`test-case-summary-failure-analysis-link-${testCaseIdentifier}`}
            >
              <div title="Analyze failure with AI">
                <span>Analyze Failure</span> <AIIcon className={classes.icon} />
              </div>
            </CleanLink>
          </Button>
        )}
        {!testCase.passed && (
          <Button>
            <CleanLink
              to={`/tests/${publicId}/suite/${testCase.testSuiteIdx}/case/${testCase.idx}/debugContext`}
              data-testid={`test-case-summary-ai-context-link-${testCaseIdentifier}`}
            >
              AI Context
            </CleanLink>
          </Button>
        )}
        {!testCase.passed && (
          <Tooltip title="Copy AI context Markdown to clipboard">
            <span>
              <IconButton
                size="small"
                onClick={copyAIContext}
                disabled={copyAIContextState === CopyAIContextState.COPYING}
                data-testid={`test-case-summary-ai-context-copy-${testCaseIdentifier}`}
              >
                <FileCopyOutlinedIcon fontSize="small" />
              </IconButton>
            </span>
          </Tooltip>
        )}
        <Fade in={copyAIContextState === CopyAIContextState.COPIED}>
          <Chip
            label="Copied to clipboard"
            variant="outlined"
            size="small"
            data-testid={`test-case-summary-ai-context-copied-${testCaseIdentifier}`}
          />
        </Fade>
        <Fade in={copyAIContextState === CopyAIContextState.ERROR}>
          <Chip
            label="Error copying AI context"
            variant="outlined"
            size="small"
            color="error"
            data-testid={`test-case-summary-ai-context-copy-error-${testCaseIdentifier}`}
          />
        </Fade>
      </AccordionActions>
    </Accordion>
  );
};

export default TestCaseFailurePanel;
