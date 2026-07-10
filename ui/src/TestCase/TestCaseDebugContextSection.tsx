import * as React from "react";
import { useEffect } from "react";
import { RouteComponentProps } from "@reach/router";
import { CopyToClipboard } from "react-copy-to-clipboard";
import { Button, Chip, Fade } from "@mui/material";
import FileCopyOutlinedIcon from "@mui/icons-material/FileCopyOutlined";
import classes from "./TestCaseDebugContextSection.module.css";
import { TestCaseDebugContext } from "../model/TestRunModel";
import { fetchTestCaseDebugContext } from "../service/TestRunService";
import LoadingState from "../Loading/LoadingState";
import LoadingSection from "../Loading/LoadingSection";

interface TestCaseDebugContextProps extends RouteComponentProps {
  publicId: string;
  testSuiteIdx: number;
  testCaseIdx: number;
}

const TestCaseDebugContextSection = ({
  publicId,
  testSuiteIdx,
  testCaseIdx,
}: TestCaseDebugContextProps) => {
  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading,
  );
  const [debugContext, setDebugContext] =
    React.useState<TestCaseDebugContext>(null);
  const [showCopied, setShowCopied] = React.useState<boolean>(false);

  useEffect(() => {
    fetchTestCaseDebugContext(publicId, testSuiteIdx, testCaseIdx)
      .then((response) => {
        setDebugContext(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  }, [setDebugContext]);

  const onCopy = () => {
    setShowCopied(true);

    setTimeout(() => setShowCopied(false), 3000);
  };

  return (
    <LoadingSection
      loadingState={loadingState}
      successComponent={
        debugContext?.markdown ? (
          <div data-testid="test-case-debug-context-section">
            <div className={classes.header}>
              <CopyToClipboard onCopy={onCopy} text={debugContext.markdown}>
                <Button
                  variant="outlined"
                  size="small"
                  startIcon={<FileCopyOutlinedIcon fontSize="small" />}
                  data-testid="test-case-debug-context-copy-button"
                >
                  Copy Markdown for AI
                </Button>
              </CopyToClipboard>
              <Fade in={showCopied}>
                <Chip
                  label="Copied to clipboard"
                  variant="outlined"
                  size="small"
                  className={classes.copied}
                  data-testid="test-case-debug-context-copied"
                />
              </Fade>
            </div>
            <div className={classes.debugContextContents}>
              <pre
                data-testid="test-case-debug-context-text"
                className={classes.debugContextText}
              >
                {debugContext.markdown}
              </pre>
            </div>
          </div>
        ) : (
          <div>Error loading test debug context</div>
        )
      }
    />
  );
};

export default TestCaseDebugContextSection;
