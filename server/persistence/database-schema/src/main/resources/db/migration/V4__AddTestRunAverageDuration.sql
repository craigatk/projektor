ALTER TABLE test_run ADD COLUMN average_duration numeric(12, 3) NOT NULL;
ALTER TABLE test_run ADD COLUMN slowest_test_case_duration numeric(12, 3) NOT NULL;