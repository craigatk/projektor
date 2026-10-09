import * as React from "react";
import { Typography } from "@mui/material";
import { SlowTestRegressions, TestCase } from "../../model/TestRunModel";
import { fetchSlowTestRegressions } from "../../service/TestRunService";
import TestCaseList from "../list/TestCaseList";
import PageTitle from "../../PageTitle";
import SlowTestRegressionSummary from "./SlowTestRegressionSummary";
import sectionClasses from "./SlowTestCasesSection.module.css";

interface SlowTestCasesDetailsProps {
  publicId: string;
  testCases: TestCase[];
}

const SlowTestCasesDetails = ({
  publicId,
  testCases,
}: SlowTestCasesDetailsProps) => {
  const [slowTestRegressions, setSlowTestRegressions] =
    React.useState<SlowTestRegressions>(null);

  React.useEffect(() => {
    fetchSlowTestRegressions(publicId)
      .then((response) => setSlowTestRegressions(response.data))
      .catch(() => setSlowTestRegressions(null));
  }, [publicId, setSlowTestRegressions]);

  const hasRegressions =
    slowTestRegressions && slowTestRegressions.regressions.length > 0;

  return (
    <div>
      <PageTitle title="Slowest test cases" testid={`slow-test-cases-title`} />
      {hasRegressions && (
        <SlowTestRegressionSummary
          publicId={publicId}
          slowTestRegressions={slowTestRegressions}
        />
      )}
      <section data-testid="slowest-test-cases">
        {/* Only needs its own header to tell it apart from the regressions */}
        {hasRegressions && (
          <Typography
            component="h2"
            variant="h6"
            className={sectionClasses.sectionTitle}
            data-testid="slowest-test-cases-section-title"
          >
            Slowest in this run
          </Typography>
        )}
        <TestCaseList
          publicId={publicId}
          testCases={testCases}
          showFullTestCaseName={true}
          showDurationFirst={true}
        />
      </section>
    </div>
  );
};

export default SlowTestCasesDetails;
