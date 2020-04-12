CREATE INDEX idx_test_failure_test_case_id on test_failure(test_case_id);

CREATE INDEX idx_test_case_test_suite_id on test_case(test_suite_id);

CREATE INDEX idx_test_suite_test_run_id on test_suite(test_run_id);
