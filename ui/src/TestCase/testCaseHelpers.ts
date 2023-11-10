import { Attachment, AttachmentType, TestCase } from "../model/TestRunModel";

const findAttachmentOfType = (
  testCase: TestCase,
  attachmentType: AttachmentType,
): Attachment | undefined =>
  testCase.attachments &&
  testCase.attachments.find(
    (attachment) => attachment.attachmentType === attachmentType,
  );

const createTestCaseIdentifier = (testCase: TestCase): string =>
  `${testCase.testSuiteIdx}-${testCase.idx}`;

export { findAttachmentOfType, createTestCaseIdentifier };
