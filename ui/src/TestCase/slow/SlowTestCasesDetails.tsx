import * as React from "react";
import { TestCase } from "../../model/TestRunModel";
import TestCaseList from "../list/TestCaseList";
import PageTitle from "../../PageTitle";

interface SlowTestCasesDetailsProps {
  publicId: string;
  testCases: TestCase[];
}

const SlowTestCasesDetails = ({
  publicId,
  testCases,
}: SlowTestCasesDetailsProps) => {
  return (
    <div>
      <PageTitle title="Slowest test cases" testid={`slow-test-cases-title`} />
      <TestCaseList
        publicId={publicId}
        testCases={testCases}
        showFullTestCaseName={true}
        showDurationFirst={true}
      />
    </div>
  );
};

export default SlowTestCasesDetails;
