enum FailureBodyType {
  COVERAGE = "COVERAGE",
  TEST_RESULTS = "TEST_RESULTS",
}

interface ResultsProcessingFailure {
  id: string;
  body: string;
  bodyType: FailureBodyType;
  createdTimestamp: Date;
  failureMessage?: string;
}

export { FailureBodyType, ResultsProcessingFailure };
