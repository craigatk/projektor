import * as React from "react";
import { PinContext } from "../Pin/PinState";
import TestRunDateItem from "./TestRunDateItem";
import moment from "moment";
import CleanLink from "../Link/CleanLink";

interface TestRunCleanupDateProps {
  createdTimestamp: Date;
}

const TestRunCleanupDate = ({ createdTimestamp }: TestRunCleanupDateProps) => {
  const {
    togglePinned,
    pinned,
    cleanupEnabled,
    maxReportAgeInDays,
  } = React.useContext(PinContext);

  if (cleanupEnabled) {
    let displayValue;
    if (pinned) {
      displayValue = (
        <span>
          Kept forever (
          <CleanLink
            to=""
            onClick={togglePinned}
            data-testid="test-run-header-unpin-link"
          >
            unpin
          </CleanLink>
          )
        </span>
      );
    } else {
      const cleanupDate = moment(createdTimestamp).add(
        maxReportAgeInDays,
        "days"
      );

      displayValue = (
        <span data-testid="test-run-report-cleanup-date">
          {cleanupDate.format("MMMM Do YYYY")} (
          <CleanLink
            to=""
            onClick={togglePinned}
            data-testid="test-run-header-pin-link"
          >
            pin
          </CleanLink>
          )
        </span>
      );
    }
    return (
      <TestRunDateItem
        label="Report cleanup"
        testId="test-run-report-cleanup"
        value={displayValue}
      />
    );
  } else {
    return null;
  }
};

export default TestRunCleanupDate;
