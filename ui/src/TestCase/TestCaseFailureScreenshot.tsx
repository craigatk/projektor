import * as React from "react";
import { AttachmentType, TestCase } from "../model/TestRunModel";
import { makeStyles } from "@material-ui/core/styles";
import {
  findAttachmentOfType,
  createTestCaseIdentifier,
} from "./testCaseHelpers";
import { RouteComponentProps } from "@reach/router";

interface TestCaseFailureScreenshotProps extends RouteComponentProps {
  testCase: TestCase;
  publicId: string;
}

const useStyles = makeStyles(() => ({
  failureScreenshot: {
    maxWidth: "100%",
  },
}));

const TestCaseFailureScreenshot = ({
  testCase,
  publicId,
}: TestCaseFailureScreenshotProps) => {
  const classes = useStyles({});

  const testCaseIdentifier = createTestCaseIdentifier(testCase);

  const screenshotAttachment = findAttachmentOfType(
    testCase,
    AttachmentType.IMAGE,
  );

  if (screenshotAttachment) {
    return (
      <img
        src={`/run/${publicId}/attachments/${screenshotAttachment.fileName}`}
        data-testid={`test-case-failure-screenshot-${testCaseIdentifier}`}
        className={classes.failureScreenshot}
        alt={`Failure screenshot for test ${testCase.name}`}
      />
    );
  } else {
    return null;
  }
};

export default TestCaseFailureScreenshot;
