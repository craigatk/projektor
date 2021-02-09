ALTER TABLE test_case ADD COLUMN system_out text;
ALTER TABLE test_case ADD COLUMN system_err text;
ALTER TABLE test_case ADD COLUMN has_system_out boolean default false;
ALTER TABLE test_case ADD COLUMN has_system_err boolean default false;
