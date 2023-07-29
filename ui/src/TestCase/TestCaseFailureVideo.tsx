import * as React from "react";
import { AttachmentType, TestCase } from "../model/TestRunModel";
import { makeStyles } from "@mui/material/styles";
import { findAttachmentOfType } from "./testCaseHelpers";
import { RouteComponentProps } from "@reach/router";

interface TestCaseFailureVideoProps extends RouteComponentProps {
  testCase: TestCase;
  publicId: string;
}

const useStyles = makeStyles(() => ({
  failureVideo: {
    maxWidth: "100%",
  },
}));

const TestCaseFailureVideo = ({
  testCase,
  publicId,
}: TestCaseFailureVideoProps) => {
  const classes = useStyles({});

  const videoAttachment = findAttachmentOfType(testCase, AttachmentType.VIDEO);

  if (videoAttachment) {
    return (
      <video controls className={classes.failureVideo}>
        <source
          src={`/run/${publicId}/attachments/${videoAttachment.fileName}`}
          data-testid={`test-case-failure-video`}
          type="video/mp4"
        />
      </video>
    );
  } else {
    return <span>No video found</span>;
  }
};

export default TestCaseFailureVideo;
