ALTER TABLE test_suite ALTER COLUMN test_run_id TYPE BIGINT;

ALTER TABLE test_case ALTER COLUMN test_suite_id TYPE BIGINT;

ALTER TABLE test_failure ALTER COLUMN test_case_id TYPE BIGINT;
